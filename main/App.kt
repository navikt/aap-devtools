import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.Topic
import no.nav.aap.kafka.vanilla.KafkaConfig
import no.nav.aap.kafka.vanilla.KafkaFactory
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes.ByteArraySerde
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

internal data class Config(val kafka: KafkaConfig)

internal object Kafka : KafkaFactory

internal fun Application.server() {
    install(ContentNegotiation) { jackson { registerModule(JavaTimeModule()) } }

    val config = loadConfig<Config>()
    val søkerTopic = Topic("aap.sokere.v1", ByteArraySerde())
    val søknadTopic = Topic("aap.soknad-sendt.v1", JsonSerde.jackson<SøknadKafkaDto>())

    val søknadProducer = Kafka.createProducer(config.kafka, søknadTopic)
    val søkerProducer = Kafka.createProducer(config.kafka, søkerTopic)

    Thread.currentThread().setUncaughtExceptionHandler { _, e -> log.error("Uhåndtert feil", e) }

    environment.monitor.subscribe(ApplicationStopping) {
        søknadProducer.close()
        søkerProducer.close()
    }

    routing {
        actuator()
        get("/delete/{personident}") {
            val personident = call.parameters.getOrFail("personident")

            søknadProducer.tombstone(søknadTopic, personident)
            søkerProducer.tombstone(søkerTopic, personident)

            call.respondText("Søknad og søker med ident $personident slettes!")
        }

        get("/søknad/{personident}") {
            val personident = call.parameters.getOrFail("personident")
            val søknad = SøknadKafkaDto()

            søknadProducer.produce(søknadTopic, personident, søknad)

            call.respondText("Søknad $søknad mottatt!")
        }
    }
}

private val secureLog: Logger = LoggerFactory.getLogger("secureLog")

private fun <V> Producer<String, V>.produce(topic: Topic<V>, key: String, value: V) {
    send(ProducerRecord(topic.name, key, value)) { meta, error ->
        if (error != null) {
            secureLog.error("Produserer til Topic feilet", error)
        } else {
            secureLog.trace(
                "Produserer til Topic",
                kv("key", key),
                kv("topic", topic.name),
                kv("partition", meta.partition()),
                kv("offset", meta.offset()),
            )
        }
    }
}

private fun <V> Producer<String, V>.tombstone(topic: Topic<V>, key: String) {
    send(ProducerRecord(topic.name, key, null)) { meta, error ->
        if (error != null) {
            secureLog.error("Tombstoner Topic feilet", error)
        } else {
            secureLog.trace(
                "Tombstoner Topic",
                kv("key", key),
                kv("topic", topic.name),
                kv("partition", meta.partition()),
                kv("offset", meta.offset()),
            )
        }
    }
}

internal fun Routing.actuator() {
    route("/actuator") {
        get("/live") {
            call.respondText("vedtak")
        }

        get("/ready") {
            call.respondText("vedtak")
        }
    }
}

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kafka.SøknadKafkaDto
import kafka.Topics
import no.nav.aap.kafka.vanilla.KafkaConfig
import no.nav.aap.kafka.vanilla.KafkaFactory
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.clients.producer.Producer
import routes.actuator
import routes.deleteSøker
import routes.sendSøknad

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

internal data class Config(val kafka: KafkaConfig)

internal object Kafka : KafkaFactory

internal fun Application.server() {
    install(ContentNegotiation) { jackson { registerModule(JavaTimeModule()) } }

    val config = loadConfig<Config>()

    val søknadProducer: Producer<String, SøknadKafkaDto> = Kafka.createProducer(config.kafka, Topics.søknad)
    val søkerProducer: Producer<String, ByteArray> = Kafka.createProducer(config.kafka, Topics.søker)

    Thread.currentThread().setUncaughtExceptionHandler { _, e -> log.error("Uhåndtert feil", e) }

    environment.monitor.subscribe(ApplicationStopping) {
        søknadProducer.close()
        søkerProducer.close()
    }

    routing {
        actuator()
        deleteSøker(søkerProducer, søknadProducer)
        sendSøknad(søknadProducer)
    }
}

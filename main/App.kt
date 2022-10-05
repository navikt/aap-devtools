import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dolly.DollyClient
import dolly.DollyConfig
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kafka.Kafka
import kafka.KafkaManager
import no.nav.aap.kafka.streams.Topic
import no.nav.aap.kafka.vanilla.KafkaConfig
import no.nav.aap.kafka.vanilla.KafkaFactory
import no.nav.aap.ktor.client.AzureConfig
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.common.serialization.Serdes
import routes.*
import routes.actuator
import routes.søker
import routes.søknad
import routes.topic

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

internal data class Config(
    val kafka: KafkaConfig,
    val dollyConfig: DollyConfig,
    val azure: AzureConfig,
)



internal fun Application.server(kafka: KafkaFactory = Kafka) {
    Thread.currentThread().setUncaughtExceptionHandler { _, e ->
        log.error("Uhåndtert feil", e)
    }

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
        }
    }

    val config = loadConfig<Config>()
    val manager = KafkaManager(config.kafka, kafka)
    val dollyClient = DollyClient(config.dollyConfig, config.azure)

    routing {
        actuator()
        søker(manager)
        søknad(manager)
        topic(manager)
        dolly(dollyClient)
    }
}

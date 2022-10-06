import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dolly.DollyClient
import dolly.DollyConfig
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kafka.Kafka
import kafka.KafkaManager
import no.nav.aap.kafka.vanilla.KafkaConfig
import no.nav.aap.kafka.vanilla.KafkaFactory
import no.nav.aap.ktor.client.AzureConfig
import no.nav.aap.ktor.config.loadConfig
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import routes.*
import routes.actuator
import routes.søker
import routes.søknad
import routes.topic

private val secureLog = LoggerFactory.getLogger("secureLog")

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

internal data class Config(
    val kafka: KafkaConfig,
    val dolly: DollyConfig,
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

    install(CallLogging) {
        level = Level.INFO
        logger = secureLog
    }

    val config = loadConfig<Config>()
    val manager = KafkaManager(config.kafka, kafka)
    val dollyClient = DollyClient(config.dolly, config.azure)

    routing {
        trace {
            secureLog.info(it.buildText())
        }
        actuator()
        søker(manager)
        søknad(manager)
        topic(manager)
        meldeplikt(manager)
        dolly(dollyClient)
    }
}

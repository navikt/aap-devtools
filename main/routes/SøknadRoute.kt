package routes

import Topics
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kafka.KafkaManager
import ktor.personident

internal fun Route.søknad(manager: KafkaManager) {
    route("/søknad/{personident}") {
        delete {
            manager.produce(
                topic = Topics.søknad,
                key = call.parameters.personident,
                value = null
            )

            call.respondText("Søknad slettet")
        }

        post {
            val søknad = call.receiveText()

            require(jackson.readTree(søknad).has("fødselsdato")) {
                "Søknad må inneholde fødselsdato"
            }

            manager.produce(
                topic = Topics.søknad,
                key = call.parameters.personident,
                value = søknad.encodeToByteArray(),
            )

            call.respondText("Søknad for mottatt")
        }
    }
}

private val jackson = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

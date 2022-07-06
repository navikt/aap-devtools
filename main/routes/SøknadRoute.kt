package routes

import Topics
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kafka.KafkaManager

internal fun Route.søknad(manager: KafkaManager) {
    route("/søknad/{personident}") {
        delete {
            val personident = call.parameters.getOrFail("personident")
            manager.produce(Topics.søknad, personident, null)
            call.respondText("Søknad $personident slettet")
        }

        post {
            val personident = call.parameters.getOrFail("personident")
            val søknad = call.receiveText()

            require(jackson.readTree(søknad).has("fødselsdato")) { "Søknad må inneholde fødselsdato" }

            manager.produce(Topics.søknad, personident, søknad)
            call.respondText("Søknad for $personident mottatt")
        }
    }
}

private val jackson = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

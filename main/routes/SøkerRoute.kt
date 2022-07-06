package routes

import Topics
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kafka.KafkaManager

internal fun Route.søker(manager: KafkaManager) {
    route("/søker/{personident}") {
        delete {
            val personident = call.parameters.getOrFail("personident")
            manager.produce(Topics.søker, personident, null)
            call.respondText("Søker $personident slettet")
        }
    }
}

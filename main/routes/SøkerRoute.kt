package routes

import Topics
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kafka.KafkaManager
import ktor.personident

internal fun Route.søker(manager: KafkaManager) {
    route("/søker/{personident}") {
        delete {
            manager.produce(
                topic = Topics.søker,
                key = call.parameters.personident,
                value = null,
            )
            call.respondText("Søker slettet")
        }
    }
}

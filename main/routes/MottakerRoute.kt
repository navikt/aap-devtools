package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kafka.KafkaManager
import kafka.Topics
import ktor.personident

internal fun Route.mottaker(manager: KafkaManager) {
    route("/mottaker/{personident}") {
        delete {
            manager.produce(
                topic = Topics.mottakere,
                key = call.parameters.personident,
                value = null,
            )
            call.respondText("Mottaker slettet")
        }
    }
}

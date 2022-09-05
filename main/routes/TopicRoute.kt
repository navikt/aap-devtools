package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kafka.*
import ktor.direction
import ktor.offset
import ktor.partition
import ktor.topic

internal fun Route.topic(manager: KafkaManager) {
    route("/topics") {
        get {
            call.respond(Topics.all.keys)
        }
    }

    route("/topic/{topic}") {
        get("/{direction}") {
            val request = KafkaRequest(
                topic = call.parameters.topic,
                direction = call.parameters.direction,
                fromEpochMillis = call.request.queryParameters["fom_ms"]?.toLong() ?: KafkaRequest.DEFAULT_EPOCH_MS,
            )

            val response: List<KafkaResult> = manager.read(request, 60)
            call.respond(response)
        }

        get("/{partition}/{offset}") {
            val request = SpecificRequest(
                topic = call.parameters.topic,
                partition = call.parameters.partition,
                offset = call.parameters.offset,
            )

            when (val response: KafkaResult? = manager.read(request)) {
                null -> call.respondText("Melding finnes ikke", status = HttpStatusCode.NotFound)
                else -> call.respond(response)
            }
        }
    }
}

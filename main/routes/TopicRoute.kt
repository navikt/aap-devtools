package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kafka.AllPartitionRequest
import kafka.KafkaManager
import kafka.KafkaResult
import kafka.SpecificRequest

internal fun Route.topic(manager: KafkaManager) {
    route("/topics") {
        get {
            call.respond(manager.topicNames())
        }
    }

    route("/topic/{topic}/{direction}") {
        get {
            val request = AllPartitionRequest(
                topic = call.parameters.getOrFail("topic"),
                direction = call.parameters.getOrFail("direction").let(::enumValueOf),
            )

            val response: List<KafkaResult> = manager.read(request)
            call.respond(response)
        }

        get("/{partition}/{offset}") {
            val request = SpecificRequest(
                topic = call.parameters.getOrFail("topic"),
                direction = call.parameters.getOrFail("direction").let(::enumValueOf),
                partition = call.parameters.getOrFail("partition").toInt(),
                offset = call.parameters.getOrFail("offset").toLong()
            )

            when (val response: KafkaResult? = manager.lookup(request)) {
                null -> call.respondText("Melding finnes ikke", status = HttpStatusCode.NotFound)
                else -> call.respond(response)
            }
        }
    }
}

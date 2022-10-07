package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kafka.KafkaManager
import kafka.Topics
import ktor.personident

internal fun Route.deleteAll(manager: KafkaManager) {

    route("/{personident}") {
        delete {
            val personident = call.parameters.personident
            val topicsToTombstone by lazy { listOf(Topics.søknad, Topics.søker, Topics.mottakere, Topics.vedtak) }
            val futures = topicsToTombstone.associateWith { manager.produce(it, personident, null) }

            val tombstoneWithStatus = futures.map { (key, future) ->
                val deleted = try {
                    future.get()
                    true
                } catch (e: Exception) {
                    false
                }
                TombstoneStatus(key.name, deleted)
            }

            call.respond(tombstoneWithStatus)
        }
    }
}

data class TombstoneStatus(
    val topic: String,
    val deleted: Boolean
)

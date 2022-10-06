package routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kafka.KafkaManager
import kafka.Topics
import ktor.personident

internal fun Route.meldeplikt(manager: KafkaManager) {
    route("/meldeplikt/{personident}") {
        post {
            val hendelse = call.receiveText()

            manager.produce(
                topic = Topics.meldeplikt,
                key = call.parameters.personident,
                value = hendelse.encodeToByteArray()
            )

            call.respondText("Meldeplikthendelse mottatt")
        }
    }
}

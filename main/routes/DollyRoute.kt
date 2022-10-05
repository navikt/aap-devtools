package routes

import dolly.DollyClient
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

internal fun Route.dolly(client: DollyClient) {
    route("/dolly") {
        get {
            call.respond(client.hentBrukere())
        }
    }
}
package routes

import dolly.DollyClient
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private const val AAP_SØKERE = "4946"

internal fun Route.dolly(client: DollyClient) {
    route("/dolly") {
        get {
            val gruppeId = call.parameters["gruppe"] ?: AAP_SØKERE
            call.respond(client.hentBrukere(gruppeId))
        }
    }
}

package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

internal fun Routing.actuator() {
    route("/actuator") {
        get("/live") {
            call.respondText("devtools")
        }

        get("/ready") {
            call.respondText("devtools")
        }
    }
}

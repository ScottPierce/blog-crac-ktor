package dev.scottpierce

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configure application routing.
 */
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, CRaC!")
        }

        get("/health") {
            call.respondText("OK")
        }
    }
}

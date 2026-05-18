package dev.portfolio.warehouse.print

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.serialization.jackson.jackson

fun main() {
    embeddedServer(Netty, port = 4020) {
        install(ContentNegotiation) {
            jackson()
        }
        routing {
            get("/health") {
                call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
            }
        }
    }.start(wait = true)
}


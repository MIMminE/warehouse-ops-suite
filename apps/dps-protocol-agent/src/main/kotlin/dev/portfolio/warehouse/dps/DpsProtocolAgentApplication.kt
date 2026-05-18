package dev.portfolio.warehouse.dps

import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText

fun main() {
    embeddedServer(Netty, port = 4030) {
        install(WebSockets)
        routing {
            webSocket("/ws/dps") {
                send(Frame.Text("""{"type":"DPS_AGENT_CONNECTED"}"""))
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        send(Frame.Text("""{"type":"DPS_AGENT_ECHO","payload":${frame.readText()}}"""))
                    }
                }
            }
        }
    }.start(wait = true)
}


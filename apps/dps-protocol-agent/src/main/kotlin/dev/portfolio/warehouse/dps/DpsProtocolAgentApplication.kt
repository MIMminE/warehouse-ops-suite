package dev.portfolio.warehouse.dps

import dev.portfolio.warehouse.dps.protocol.AgentEnvelope
import dev.portfolio.warehouse.dps.protocol.DpsProtocolService
import dev.portfolio.warehouse.dps.protocol.StartPickingBatchPayload
import dev.portfolio.warehouse.dps.simulator.ConfirmCellRequest
import dev.portfolio.warehouse.dps.simulator.DpsSimulatorService
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText

fun main() {
    val dpsProtocolService = DpsProtocolService()
    val simulatorService = DpsSimulatorService(dpsProtocolService)

    embeddedServer(Netty, port = 4030) {
        install(ContentNegotiation) {
            jackson {
                findAndRegisterModules()
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            }
        }
        install(WebSockets)
        routing {
            get("/health") {
                call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
            }
            get("/simulator/state") {
                call.respond(simulatorService.snapshot())
            }
            post("/simulator/cells/{cellCode}/confirm") {
                val cellCode = call.parameters["cellCode"].orEmpty()
                val request = call.receive<ConfirmCellRequest>()
                call.respond(simulatorService.confirm(cellCode, request))
            }
            webSocket("/ws/dps") {
                send(Frame.Text(dpsProtocolService.connectedMessage()))
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val envelope = dpsProtocolService.decode(frame.readText())
                        val response = when (envelope.type) {
                            "PICKING_BATCH_STARTED" -> {
                                val payload = dpsProtocolService.convertPayload(
                                    envelope.payload,
                                    StartPickingBatchPayload::class.java,
                                )
                                dpsProtocolService.startBatch(envelope.requestId, payload)
                            }
                            "PICKING_BATCH_CANCELED" -> dpsProtocolService.cancelBatch(envelope.requestId)
                            "DPS_AGENT_STATUS_REQUESTED" -> dpsProtocolService.status(envelope.requestId)
                            else -> AgentEnvelope.error(
                                requestId = envelope.requestId,
                                command = envelope.type,
                                message = "지원하지 않는 DPS 명령입니다: ${envelope.type}",
                            )
                        }
                        send(Frame.Text(dpsProtocolService.encode(response)))
                    }
                }
            }
        }
    }.start(wait = true)
}

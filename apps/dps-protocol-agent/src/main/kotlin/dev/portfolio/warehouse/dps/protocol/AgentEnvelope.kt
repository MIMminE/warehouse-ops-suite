package dev.portfolio.warehouse.dps.protocol

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant

data class AgentEnvelope(
    val type: String,
    val requestId: String? = null,
    val payload: JsonNode? = null,
    val sentAt: Instant = Instant.now(),
) {
    companion object {
        fun event(
            type: String,
            requestId: String? = null,
            payload: Any? = null,
        ): AgentOutboundEnvelope = AgentOutboundEnvelope(
            type = type,
            requestId = requestId,
            payload = payload,
        )

        fun error(
            requestId: String?,
            command: String?,
            message: String,
        ): AgentOutboundEnvelope = AgentOutboundEnvelope(
            type = "DPS_AGENT_ERROR",
            requestId = requestId,
            payload = AgentErrorPayload(command = command, message = message),
        )
    }
}

data class AgentOutboundEnvelope(
    val type: String,
    val requestId: String? = null,
    val payload: Any? = null,
    val sentAt: Instant = Instant.now(),
)

data class AgentErrorPayload(
    val command: String?,
    val message: String,
)


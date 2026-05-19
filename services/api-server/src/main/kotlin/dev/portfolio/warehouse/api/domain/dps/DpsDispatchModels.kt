package dev.portfolio.warehouse.api.domain.dps

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant

data class DpsDispatchResponse(
    val waveId: Long,
    val waveNo: String,
    val requestId: String,
    val agentResponseType: String,
    val cellCount: Int,
    val sentAt: Instant,
)

data class DpsAgentEnvelope(
    val type: String,
    val requestId: String,
    val payload: Any,
    val sentAt: Instant = Instant.now(),
)

data class DpsAgentInboundEnvelope(
    val type: String,
    val requestId: String?,
    val payload: JsonNode?,
    val sentAt: Instant?,
)

data class DpsStartPickingBatchPayload(
    val batchId: String,
    val cells: List<DpsPickingCellCommand>,
)

data class DpsPickingCellCommand(
    val cellCode: String,
    val skuCode: String,
    val quantity: Int,
)

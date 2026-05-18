package dev.portfolio.warehouse.dps.protocol

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class DpsProtocolService {
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()
    private var activeBatchId: String? = null
    private val cells = linkedMapOf<String, DpsCellRuntimeState>()

    fun connectedMessage(): String =
        encode(
            AgentEnvelope.event(
                type = "DPS_AGENT_CONNECTED",
                payload = mapOf(
                    "agentName" to "warehouse-dps-protocol-agent",
                    "websocketPath" to "/ws/dps",
                ),
            ),
        )

    fun decode(rawMessage: String): AgentEnvelope =
        objectMapper.readValue(rawMessage, AgentEnvelope::class.java)

    fun encode(message: AgentOutboundEnvelope): String =
        objectMapper.writeValueAsString(message)

    fun <T> convertPayload(payload: JsonNode?, type: Class<T>): T =
        objectMapper.treeToValue(requireNotNull(payload) { "payload is required" }, type)

    fun startBatch(
        requestId: String?,
        payload: StartPickingBatchPayload,
    ): AgentOutboundEnvelope {
        require(payload.batchId.isNotBlank()) { "batchId must not be blank" }
        require(payload.cells.isNotEmpty()) { "cells must not be empty" }

        activeBatchId = payload.batchId
        cells.clear()
        payload.cells.forEach { cell ->
            cells[cell.cellCode] = DpsCellRuntimeState(
                cellCode = cell.cellCode,
                skuCode = cell.skuCode,
                quantity = cell.quantity,
                state = CellState.LIGHT_ON,
            )
        }

        return AgentEnvelope.event(
            type = "PICKING_BATCH_ACCEPTED",
            requestId = requestId,
            payload = PickingBatchAcceptedPayload(
                batchId = payload.batchId,
                cellCount = payload.cells.size,
            ),
        )
    }

    fun cancelBatch(requestId: String?): AgentOutboundEnvelope {
        val canceledBatchId = activeBatchId
        activeBatchId = null
        cells.clear()
        return AgentEnvelope.event(
            type = "PICKING_BATCH_CANCELED",
            requestId = requestId,
            payload = mapOf("batchId" to canceledBatchId),
        )
    }

    fun status(requestId: String?): AgentOutboundEnvelope =
        AgentEnvelope.event(
            type = "DPS_AGENT_STATUS",
            requestId = requestId,
            payload = snapshot(),
        )

    fun confirmCell(
        cellCode: String,
        pickedQuantity: Int,
    ): List<AgentOutboundEnvelope> {
        val batchId = activeBatchId ?: return listOf(
            AgentEnvelope.error(null, "PICKING_CONFIRMED", "활성 피킹 배치가 없습니다."),
        )
        val cell = cells[cellCode] ?: return listOf(
            AgentEnvelope.event(
                type = "CELL_LIGHT_FAILED",
                payload = CellLightFailedPayload(
                    batchId = batchId,
                    cellCode = cellCode,
                    reason = "CELL_NOT_FOUND",
                ),
            ),
        )
        if (pickedQuantity > cell.quantity) {
            cells[cellCode] = cell.copy(state = CellState.FAILED)
            return listOf(
                AgentEnvelope.event(
                    type = "CELL_LIGHT_FAILED",
                    payload = CellLightFailedPayload(
                        batchId = batchId,
                        cellCode = cellCode,
                        reason = "PICKED_QUANTITY_EXCEEDED",
                    ),
                ),
            )
        }

        cells[cellCode] = cell.copy(state = CellState.COMPLETED)
        val events = mutableListOf(
            AgentEnvelope.event(
                type = "PICKING_CONFIRMED",
                payload = PickingConfirmedPayload(
                    batchId = batchId,
                    cellCode = cellCode,
                    skuCode = cell.skuCode,
                    pickedQuantity = pickedQuantity,
                ),
            ),
        )
        if (cells.values.all { it.state == CellState.COMPLETED }) {
            events += AgentEnvelope.event(
                type = "PICKING_BATCH_COMPLETED",
                payload = PickingBatchCompletedPayload(
                    batchId = batchId,
                    completedCellCount = cells.size,
                ),
            )
        }
        return events
    }

    fun snapshot(): DpsAgentStatusPayload =
        DpsAgentStatusPayload(
            activeBatchId = activeBatchId,
            cellCount = cells.size,
            litCellCount = cells.values.count { it.state == CellState.LIGHT_ON },
            completedCellCount = cells.values.count { it.state == CellState.COMPLETED },
            failedCellCount = cells.values.count { it.state == CellState.FAILED },
        )

    fun cellStates(): List<DpsCellRuntimeState> = cells.values.toList()
}


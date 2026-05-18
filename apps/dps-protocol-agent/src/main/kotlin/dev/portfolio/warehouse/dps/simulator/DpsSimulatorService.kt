package dev.portfolio.warehouse.dps.simulator

import dev.portfolio.warehouse.dps.protocol.AgentOutboundEnvelope
import dev.portfolio.warehouse.dps.protocol.DpsAgentStatusPayload
import dev.portfolio.warehouse.dps.protocol.DpsCellRuntimeState
import dev.portfolio.warehouse.dps.protocol.DpsProtocolService

class DpsSimulatorService(
    private val dpsProtocolService: DpsProtocolService,
) {
    fun snapshot(): DpsSimulatorSnapshot =
        DpsSimulatorSnapshot(
            status = dpsProtocolService.snapshot(),
            cells = dpsProtocolService.cellStates(),
        )

    fun confirm(
        cellCode: String,
        request: ConfirmCellRequest,
    ): ConfirmCellResponse =
        ConfirmCellResponse(
            events = dpsProtocolService.confirmCell(
                cellCode = cellCode,
                pickedQuantity = request.pickedQuantity,
            ),
        )
}

data class ConfirmCellRequest(
    val pickedQuantity: Int,
)

data class ConfirmCellResponse(
    val events: List<AgentOutboundEnvelope>,
)

data class DpsSimulatorSnapshot(
    val status: DpsAgentStatusPayload,
    val cells: List<DpsCellRuntimeState>,
)


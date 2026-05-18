package dev.portfolio.warehouse.dps.protocol

data class StartPickingBatchPayload(
    val batchId: String,
    val cells: List<PickingCellCommand>,
)

data class PickingCellCommand(
    val cellCode: String,
    val skuCode: String,
    val quantity: Int,
)

data class PickingBatchAcceptedPayload(
    val batchId: String,
    val cellCount: Int,
)

data class DpsAgentStatusPayload(
    val activeBatchId: String?,
    val cellCount: Int,
    val litCellCount: Int,
    val completedCellCount: Int,
    val failedCellCount: Int,
)

data class CellLightRequestedPayload(
    val batchId: String,
    val cellCode: String,
    val skuCode: String,
    val quantity: Int,
)

data class PickingConfirmedPayload(
    val batchId: String,
    val cellCode: String,
    val skuCode: String,
    val pickedQuantity: Int,
)

data class PickingBatchCompletedPayload(
    val batchId: String,
    val completedCellCount: Int,
)

data class CellLightFailedPayload(
    val batchId: String,
    val cellCode: String,
    val reason: String,
)

enum class CellState {
    IDLE,
    LIGHT_ON,
    COMPLETED,
    FAILED,
}

data class DpsCellRuntimeState(
    val cellCode: String,
    val skuCode: String,
    val quantity: Int,
    val state: CellState,
)


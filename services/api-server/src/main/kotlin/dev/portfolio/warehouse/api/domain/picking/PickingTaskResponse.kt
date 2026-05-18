package dev.portfolio.warehouse.api.domain.picking

data class PickingTaskResponse(
    val id: Long,
    val taskNo: String,
    val outboundWaveId: Long,
    val outboundOrderLineId: Long?,
    val sourceLocationId: Long,
    val skuId: Long,
    val status: PickingTaskStatus,
    val requestedQuantity: Int,
    val pickedQuantity: Int,
)

fun PickingTaskEntity.toResponse(): PickingTaskResponse =
    PickingTaskResponse(
        id = requireNotNull(id),
        taskNo = taskNo,
        outboundWaveId = requireNotNull(outboundWave.id),
        outboundOrderLineId = outboundOrderLine?.id,
        sourceLocationId = requireNotNull(sourceLocation.id),
        skuId = requireNotNull(sku.id),
        status = status,
        requestedQuantity = requestedQuantity,
        pickedQuantity = pickedQuantity,
    )


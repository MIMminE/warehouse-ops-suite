package dev.portfolio.warehouse.api.domain.putaway

data class PutawayTaskResponse(
    val id: Long,
    val taskNo: String,
    val receivingOrderLineId: Long,
    val warehouseId: Long,
    val targetLocationId: Long,
    val skuId: Long,
    val status: PutawayTaskStatus,
    val putawayQuantity: Int,
    val assignedWorker: String?,
)

fun PutawayTaskEntity.toResponse(): PutawayTaskResponse =
    PutawayTaskResponse(
        id = requireNotNull(id),
        taskNo = taskNo,
        receivingOrderLineId = requireNotNull(receivingOrderLine.id),
        warehouseId = requireNotNull(warehouse.id),
        targetLocationId = requireNotNull(targetLocation.id),
        skuId = requireNotNull(sku.id),
        status = status,
        putawayQuantity = putawayQuantity,
        assignedWorker = assignedWorker,
    )


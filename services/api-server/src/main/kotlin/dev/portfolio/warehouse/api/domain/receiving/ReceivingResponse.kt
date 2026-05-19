package dev.portfolio.warehouse.api.domain.receiving

data class ReceivingOrderResponse(
    val id: Long,
    val receivingNo: String,
    val clientCompanyId: Long,
    val clientCompanyName: String,
    val warehouseId: Long,
    val warehouseName: String,
    val status: ReceivingOrderStatus,
    val supplierName: String?,
    val requestedBy: String,
    val requestedQuantity: Int,
    val receivedQuantity: Int,
    val putawayQuantity: Int,
    val createdAt: String,
    val lines: List<ReceivingOrderLineResponse>,
)

data class ReceivingOrderLineResponse(
    val id: Long,
    val lineNo: Int,
    val skuId: Long,
    val skuCode: String,
    val skuName: String,
    val requestedQuantity: Int,
    val receivedQuantity: Int,
    val putawayQuantity: Int,
)

fun ReceivingOrderEntity.toResponse(
    lines: List<ReceivingOrderLineEntity>,
    putawayQuantityByLineId: Map<Long, Int> = emptyMap(),
): ReceivingOrderResponse =
    ReceivingOrderResponse(
        id = requireNotNull(id),
        receivingNo = receivingNo,
        clientCompanyId = requireNotNull(clientCompany.id),
        clientCompanyName = clientCompany.name,
        warehouseId = requireNotNull(warehouse.id),
        warehouseName = warehouse.name,
        status = status,
        supplierName = supplierName,
        requestedBy = requestedBy,
        requestedQuantity = lines.sumOf { it.requestedQuantity },
        receivedQuantity = lines.sumOf { it.receivedQuantity },
        putawayQuantity = lines.sumOf { putawayQuantityByLineId[requireNotNull(it.id)] ?: 0 },
        createdAt = createdAt.toLocalDate().toString(),
        lines = lines.map { it.toResponse(putawayQuantityByLineId[requireNotNull(it.id)] ?: 0) },
    )

fun ReceivingOrderLineEntity.toResponse(putawayQuantity: Int = 0): ReceivingOrderLineResponse =
    ReceivingOrderLineResponse(
        id = requireNotNull(id),
        lineNo = lineNo,
        skuId = requireNotNull(sku.id),
        skuCode = sku.code,
        skuName = sku.name,
        requestedQuantity = requestedQuantity,
        receivedQuantity = receivedQuantity,
        putawayQuantity = putawayQuantity,
    )

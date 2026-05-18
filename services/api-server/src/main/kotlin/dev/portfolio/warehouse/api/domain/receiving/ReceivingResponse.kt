package dev.portfolio.warehouse.api.domain.receiving

data class ReceivingOrderResponse(
    val id: Long,
    val receivingNo: String,
    val clientCompanyId: Long,
    val warehouseId: Long,
    val status: ReceivingOrderStatus,
    val lines: List<ReceivingOrderLineResponse>,
)

data class ReceivingOrderLineResponse(
    val id: Long,
    val lineNo: Int,
    val skuId: Long,
    val skuCode: String,
    val requestedQuantity: Int,
    val receivedQuantity: Int,
)

fun ReceivingOrderEntity.toResponse(lines: List<ReceivingOrderLineEntity>): ReceivingOrderResponse =
    ReceivingOrderResponse(
        id = requireNotNull(id),
        receivingNo = receivingNo,
        clientCompanyId = requireNotNull(clientCompany.id),
        warehouseId = requireNotNull(warehouse.id),
        status = status,
        lines = lines.map { it.toResponse() },
    )

fun ReceivingOrderLineEntity.toResponse(): ReceivingOrderLineResponse =
    ReceivingOrderLineResponse(
        id = requireNotNull(id),
        lineNo = lineNo,
        skuId = requireNotNull(sku.id),
        skuCode = sku.code,
        requestedQuantity = requestedQuantity,
        receivedQuantity = receivedQuantity,
    )


package dev.portfolio.warehouse.api.domain.outboundorder

import java.time.LocalDate
import java.time.LocalDateTime

data class OutboundOrderResponse(
    val id: Long,
    val clientCompanyId: Long,
    val warehouseId: Long,
    val outboundOrderNo: String,
    val externalReferenceNo: String?,
    val intakeSource: OutboundOrderIntakeSource,
    val status: OutboundOrderStatus,
    val receiverName: String,
    val receiverPhone: String,
    val zipCode: String,
    val address1: String,
    val address2: String?,
    val deliveryMemo: String?,
    val requestedShipDate: LocalDate?,
    val orderedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
)

data class OutboundOrderDetailResponse(
    val order: OutboundOrderResponse,
    val lines: List<OutboundOrderLineResponse>,
)

data class OutboundOrderLineResponse(
    val id: Long,
    val lineNo: Int,
    val skuId: Long,
    val skuCode: String,
    val skuName: String,
    val orderedQuantity: Int,
    val allocatedQuantity: Int,
    val pickedQuantity: Int,
    val packedQuantity: Int,
)

fun OutboundOrderEntity.toResponse(): OutboundOrderResponse =
    OutboundOrderResponse(
        id = requireNotNull(id),
        clientCompanyId = requireNotNull(clientCompany.id),
        warehouseId = requireNotNull(warehouse.id),
        outboundOrderNo = outboundOrderNo,
        externalReferenceNo = externalReferenceNo,
        intakeSource = intakeSource,
        status = status,
        receiverName = receiverName,
        receiverPhone = receiverPhone,
        zipCode = zipCode,
        address1 = address1,
        address2 = address2,
        deliveryMemo = deliveryMemo,
        requestedShipDate = requestedShipDate,
        orderedAt = orderedAt,
        createdAt = createdAt,
    )

fun OutboundOrderLineEntity.toResponse(): OutboundOrderLineResponse =
    OutboundOrderLineResponse(
        id = requireNotNull(id),
        lineNo = lineNo,
        skuId = requireNotNull(sku.id),
        skuCode = sku.code,
        skuName = sku.name,
        orderedQuantity = orderedQuantity,
        allocatedQuantity = allocatedQuantity,
        pickedQuantity = pickedQuantity,
        packedQuantity = packedQuantity,
    )


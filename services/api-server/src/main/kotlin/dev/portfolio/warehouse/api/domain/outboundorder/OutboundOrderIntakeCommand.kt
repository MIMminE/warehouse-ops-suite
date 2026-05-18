package dev.portfolio.warehouse.api.domain.outboundorder

import java.time.LocalDate
import java.time.LocalDateTime

data class OutboundOrderIntakeCommand(
    val clientCompanyId: Long,
    val warehouseId: Long,
    val outboundOrderNo: String,
    val externalReferenceNo: String?,
    val receiver: OutboundReceiverCommand,
    val requestedShipDate: LocalDate?,
    val orderedAt: LocalDateTime?,
    val lines: List<OutboundOrderLineCommand>,
)

data class OutboundReceiverCommand(
    val name: String,
    val phone: String,
    val zipCode: String,
    val address1: String,
    val address2: String?,
    val deliveryMemo: String?,
)

data class OutboundOrderLineCommand(
    val lineNo: Int,
    val skuId: Long,
    val orderedQuantity: Int,
)


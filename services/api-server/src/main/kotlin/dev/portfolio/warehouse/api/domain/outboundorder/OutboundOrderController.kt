package dev.portfolio.warehouse.api.domain.outboundorder

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/outbound-orders")
class OutboundOrderController(
    private val outboundOrderIntakePort: OutboundOrderIntakePort,
    private val outboundOrderQueryService: OutboundOrderQueryService,
    private val outboundOrderAllocationService: OutboundOrderAllocationService,
) {
    @GetMapping
    fun search(
        @RequestParam(required = false) clientCompanyId: Long?,
        @RequestParam(required = false) status: OutboundOrderStatus?,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @RequestParam(required = false)
        requestedShipDateFrom: LocalDate?,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @RequestParam(required = false)
        requestedShipDateTo: LocalDate?,
    ): List<OutboundOrderResponse> =
        outboundOrderQueryService.search(
            clientCompanyId = clientCompanyId,
            status = status,
            requestedShipDateFrom = requestedShipDateFrom,
            requestedShipDateTo = requestedShipDateTo,
        )

    @GetMapping("/{orderId}")
    fun getDetail(
        @PathVariable orderId: Long,
    ): OutboundOrderDetailResponse =
        outboundOrderQueryService.getDetail(orderId)

    @PostMapping("/{orderId}/allocate")
    fun allocate(
        @PathVariable orderId: Long,
    ): OutboundOrderDetailResponse =
        outboundOrderAllocationService.allocate(orderId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateOutboundOrderRequest,
    ): OutboundOrderResponse =
        outboundOrderIntakePort.receive(request.toCommand()).toResponse()
}

data class CreateOutboundOrderRequest(
    @field:NotNull
    val clientCompanyId: Long?,
    @field:NotNull
    val warehouseId: Long?,
    @field:NotBlank
    val outboundOrderNo: String?,
    val externalReferenceNo: String?,
    @field:Valid
    @field:NotNull
    val receiver: OutboundReceiverRequest?,
    val requestedShipDate: LocalDate?,
    val orderedAt: LocalDateTime?,
    @field:Valid
    @field:NotEmpty
    val lines: List<OutboundOrderLineRequest>,
) {
    fun toCommand(): OutboundOrderIntakeCommand =
        OutboundOrderIntakeCommand(
            clientCompanyId = requireNotNull(clientCompanyId),
            warehouseId = requireNotNull(warehouseId),
            outboundOrderNo = requireNotNull(outboundOrderNo),
            externalReferenceNo = externalReferenceNo,
            receiver = requireNotNull(receiver).toCommand(),
            requestedShipDate = requestedShipDate,
            orderedAt = orderedAt,
            lines = lines.map { it.toCommand() },
        )
}

data class OutboundReceiverRequest(
    @field:NotBlank
    val name: String?,
    @field:NotBlank
    val phone: String?,
    @field:NotBlank
    val zipCode: String?,
    @field:NotBlank
    val address1: String?,
    val address2: String?,
    val deliveryMemo: String?,
) {
    fun toCommand(): OutboundReceiverCommand =
        OutboundReceiverCommand(
            name = requireNotNull(name),
            phone = requireNotNull(phone),
            zipCode = requireNotNull(zipCode),
            address1 = requireNotNull(address1),
            address2 = address2,
            deliveryMemo = deliveryMemo,
        )
}

data class OutboundOrderLineRequest(
    @field:NotNull
    val lineNo: Int?,
    @field:NotNull
    val skuId: Long?,
    @field:Min(1)
    val orderedQuantity: Int,
) {
    fun toCommand(): OutboundOrderLineCommand =
        OutboundOrderLineCommand(
            lineNo = requireNotNull(lineNo),
            skuId = requireNotNull(skuId),
            orderedQuantity = orderedQuantity,
        )
}

package dev.portfolio.warehouse.api.domain.receiving

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/receiving-orders")
class ReceivingController(
    private val receivingService: ReceivingService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateReceivingOrderRequest,
    ): ReceivingOrderResponse =
        receivingService.create(request)

    @PostMapping("/lines/{lineId}/receive")
    fun receiveLine(
        @PathVariable lineId: Long,
        @Valid @RequestBody request: ReceiveReceivingLineRequest,
    ): ReceivingOrderLineResponse =
        receivingService.receiveLine(lineId, request.receivedQuantity)
}

data class CreateReceivingOrderRequest(
    @field:NotBlank
    val receivingNo: String,
    @field:NotNull
    val clientCompanyId: Long,
    @field:NotNull
    val warehouseId: Long,
    val supplierName: String?,
    @field:NotBlank
    val requestedBy: String,
    val memo: String?,
    @field:Valid
    @field:NotEmpty
    val lines: List<CreateReceivingOrderLineRequest>,
)

data class CreateReceivingOrderLineRequest(
    @field:NotNull
    val lineNo: Int,
    @field:NotNull
    val skuId: Long,
    @field:Min(1)
    val requestedQuantity: Int,
)

data class ReceiveReceivingLineRequest(
    @field:Min(1)
    val receivedQuantity: Int,
)


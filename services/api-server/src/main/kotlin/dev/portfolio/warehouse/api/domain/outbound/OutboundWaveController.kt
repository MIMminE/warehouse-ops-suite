package dev.portfolio.warehouse.api.domain.outbound

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/outbound-waves")
class OutboundWaveController(
    private val outboundWaveService: OutboundWaveService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateOutboundWaveRequest,
    ): OutboundWaveResponse =
        outboundWaveService.create(request)
}

data class CreateOutboundWaveRequest(
    @field:NotBlank
    val waveNo: String,
    @field:NotNull
    val clientCompanyId: Long,
    @field:NotNull
    val warehouseId: Long,
    @field:NotBlank
    val requestedBy: String,
    val memo: String?,
    @field:NotEmpty
    val outboundOrderLineIds: List<Long>,
)


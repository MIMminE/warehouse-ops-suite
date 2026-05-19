package dev.portfolio.warehouse.api.domain.outbound

import jakarta.validation.Valid
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

@RestController
@RequestMapping("/api/outbound-waves")
class OutboundWaveController(
    private val outboundWaveService: OutboundWaveService,
) {
    @GetMapping
    fun search(
        @RequestParam(required = false) clientCompanyId: Long?,
        @RequestParam(required = false) warehouseId: Long?,
        @RequestParam(required = false) status: OutboundWaveStatus?,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @RequestParam(required = false)
        createdFrom: LocalDate?,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @RequestParam(required = false)
        createdTo: LocalDate?,
    ): List<OutboundWaveResponse> =
        outboundWaveService.search(
            clientCompanyId = clientCompanyId,
            warehouseId = warehouseId,
            status = status,
            createdFrom = createdFrom,
            createdTo = createdTo,
        )

    @GetMapping("/{waveId}")
    fun getDetail(
        @PathVariable waveId: Long,
    ): OutboundWaveResponse =
        outboundWaveService.getDetail(waveId)

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

package dev.portfolio.warehouse.api.domain.dps

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/outbound-waves")
class DpsDispatchController(
    private val dpsDispatchService: DpsDispatchService,
) {
    @PostMapping("/{waveId}/dispatch-dps")
    fun dispatchWave(
        @PathVariable waveId: Long,
    ): DpsDispatchResponse =
        dpsDispatchService.dispatchWave(waveId)
}

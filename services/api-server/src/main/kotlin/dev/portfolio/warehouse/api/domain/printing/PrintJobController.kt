package dev.portfolio.warehouse.api.domain.printing

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class PrintJobController(
    private val printJobService: PrintJobService,
) {
    @GetMapping("/outbound-waves/{waveId}/print-jobs")
    fun findByWave(
        @PathVariable waveId: Long,
    ): List<PrintJobResponse> =
        printJobService.findByWave(waveId)

    @PostMapping("/outbound-waves/{waveId}/print-jobs/picking-list")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun requestPickingListPrint(
        @PathVariable waveId: Long,
        @RequestBody request: CreatePickingListPrintJobRequest,
    ): PrintJobResponse =
        printJobService.requestPickingListPrint(waveId, request)
}

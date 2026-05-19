package dev.portfolio.warehouse.api.domain.printing

import dev.portfolio.warehouse.api.domain.outbound.OutboundWaveRepository
import dev.portfolio.warehouse.api.support.error.NotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class PrintJobService(
    private val outboundWaveRepository: OutboundWaveRepository,
    private val printJobRepository: PrintJobRepository,
    private val printAgentClient: PrintAgentClient,
    @Value("\${app.print.pdf-renderer-url:http://localhost:4050}")
    private val pdfRendererUrl: String,
) {
    @Transactional(readOnly = true)
    fun findByWave(waveId: Long): List<PrintJobResponse> =
        printJobRepository.findByOutboundWaveIdOrderByIdDesc(waveId)
            .map { it.toResponse() }

    @Transactional
    fun requestPickingListPrint(
        waveId: Long,
        request: CreatePickingListPrintJobRequest,
    ): PrintJobResponse {
        val wave = outboundWaveRepository.findById(waveId)
            .orElseThrow { NotFoundException("출고 웨이브를 찾을 수 없습니다: $waveId") }
        val now = LocalDateTime.now()
        val job = printJobRepository.save(
            PrintJobEntity(
                jobNo = "PRT-${DateTimeFormatter.ofPattern("yyMMddHHmmss").format(now)}-${waveId}",
                outboundWave = wave,
                status = PrintJobStatus.REQUESTED,
                documentType = DocumentType.PICKING_LIST,
                printerName = request.printerName,
            ),
        )

        val agentResponse = printAgentClient.enqueue(
            PrintAgentRequest(
                jobNo = job.jobNo,
                documentType = job.documentType.name,
                documentUrl = "$pdfRendererUrl/samples/picking-list",
                printerName = request.printerName,
            ),
        )

        job.status = when (agentResponse.status) {
            "QUEUED" -> PrintJobStatus.QUEUED
            "FAILED" -> PrintJobStatus.FAILED
            else -> PrintJobStatus.REQUESTED
        }
        job.failureReason = agentResponse.failureReason
        job.updatedAt = LocalDateTime.now()
        return job.toResponse()
    }
}

data class CreatePickingListPrintJobRequest(
    val printerName: String = "HP-LaserJet-PICK",
)

package dev.portfolio.warehouse.api.domain.printing

import java.time.LocalDateTime

data class PrintJobResponse(
    val id: Long,
    val jobNo: String,
    val outboundWaveId: Long?,
    val outboundWaveNo: String?,
    val pickingTaskId: Long?,
    val pickingTaskNo: String?,
    val status: PrintJobStatus,
    val documentType: DocumentType,
    val printerName: String?,
    val failureReason: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

fun PrintJobEntity.toResponse(): PrintJobResponse =
    PrintJobResponse(
        id = requireNotNull(id),
        jobNo = jobNo,
        outboundWaveId = outboundWave?.id,
        outboundWaveNo = outboundWave?.waveNo,
        pickingTaskId = pickingTask?.id,
        pickingTaskNo = pickingTask?.taskNo,
        status = status,
        documentType = documentType,
        printerName = printerName,
        failureReason = failureReason,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

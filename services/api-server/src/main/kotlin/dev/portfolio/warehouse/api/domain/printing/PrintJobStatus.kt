package dev.portfolio.warehouse.api.domain.printing

enum class PrintJobStatus {
    REQUESTED,
    QUEUED,
    PRINTING,
    PRINTED,
    FAILED,
    CANCELED,
}


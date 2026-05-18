package dev.portfolio.warehouse.api.domain.outbound

enum class OutboundWaveStatus {
    READY,
    ALLOCATED,
    PICKING,
    COMPLETED,
    CANCELED,
}


package dev.portfolio.warehouse.api.domain.outboundorder

enum class OutboundOrderStatus {
    RECEIVED,
    ALLOCATED,
    WAVE_ASSIGNED,
    PICKING,
    PACKING,
    READY_TO_SHIP,
    SHIPPED,
    CANCELED,
}


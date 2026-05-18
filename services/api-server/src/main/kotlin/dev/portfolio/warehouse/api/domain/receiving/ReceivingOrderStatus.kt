package dev.portfolio.warehouse.api.domain.receiving

enum class ReceivingOrderStatus {
    DRAFT,
    REQUESTED,
    RECEIVING,
    PUTAWAY,
    COMPLETED,
    CANCELED,
}


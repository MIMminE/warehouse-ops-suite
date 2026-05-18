package dev.portfolio.warehouse.api.domain.outboundorder

interface OutboundOrderIntakePort {
    val source: OutboundOrderIntakeSource

    fun receive(command: OutboundOrderIntakeCommand): OutboundOrderEntity
}


package dev.portfolio.warehouse.api.domain.outboundorder

import org.springframework.data.jpa.repository.JpaRepository

interface OutboundOrderRepository : JpaRepository<OutboundOrderEntity, Long> {
    fun existsByClientCompanyIdAndOutboundOrderNo(clientCompanyId: Long, outboundOrderNo: String): Boolean
}


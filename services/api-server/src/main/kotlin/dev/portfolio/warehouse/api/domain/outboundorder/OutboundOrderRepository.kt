package dev.portfolio.warehouse.api.domain.outboundorder

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface OutboundOrderRepository : JpaRepository<OutboundOrderEntity, Long>, JpaSpecificationExecutor<OutboundOrderEntity> {
    fun existsByClientCompanyIdAndOutboundOrderNo(clientCompanyId: Long, outboundOrderNo: String): Boolean
}

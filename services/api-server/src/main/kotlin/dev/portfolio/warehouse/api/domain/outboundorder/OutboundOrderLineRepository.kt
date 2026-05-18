package dev.portfolio.warehouse.api.domain.outboundorder

import org.springframework.data.jpa.repository.JpaRepository

interface OutboundOrderLineRepository : JpaRepository<OutboundOrderLineEntity, Long>


package dev.portfolio.warehouse.api.domain.receiving

import org.springframework.data.jpa.repository.JpaRepository

interface ReceivingOrderLineRepository : JpaRepository<ReceivingOrderLineEntity, Long> {
    fun findByReceivingOrderIdOrderByLineNo(receivingOrderId: Long): List<ReceivingOrderLineEntity>
}

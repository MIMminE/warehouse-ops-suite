package dev.portfolio.warehouse.api.domain.receiving

import org.springframework.data.jpa.repository.JpaRepository

interface ReceivingOrderRepository : JpaRepository<ReceivingOrderEntity, Long> {
    fun existsByReceivingNo(receivingNo: String): Boolean
}


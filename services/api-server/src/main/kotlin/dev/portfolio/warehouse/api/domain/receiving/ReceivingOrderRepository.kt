package dev.portfolio.warehouse.api.domain.receiving

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ReceivingOrderRepository : JpaRepository<ReceivingOrderEntity, Long>, JpaSpecificationExecutor<ReceivingOrderEntity> {
    fun existsByReceivingNo(receivingNo: String): Boolean
}

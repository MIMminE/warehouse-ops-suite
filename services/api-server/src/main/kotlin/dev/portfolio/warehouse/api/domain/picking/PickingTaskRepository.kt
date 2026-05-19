package dev.portfolio.warehouse.api.domain.picking

import org.springframework.data.jpa.repository.JpaRepository

interface PickingTaskRepository : JpaRepository<PickingTaskEntity, Long> {
    fun existsByTaskNo(taskNo: String): Boolean

    fun existsByOutboundOrderLineId(outboundOrderLineId: Long): Boolean

    fun findByOutboundWaveIdOrderById(outboundWaveId: Long): List<PickingTaskEntity>
}

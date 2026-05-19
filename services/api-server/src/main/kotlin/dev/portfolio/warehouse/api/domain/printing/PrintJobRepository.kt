package dev.portfolio.warehouse.api.domain.printing

import org.springframework.data.jpa.repository.JpaRepository

interface PrintJobRepository : JpaRepository<PrintJobEntity, Long> {
    fun existsByJobNo(jobNo: String): Boolean

    fun findByOutboundWaveIdOrderByIdDesc(outboundWaveId: Long): List<PrintJobEntity>
}

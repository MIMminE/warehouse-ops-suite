package dev.portfolio.warehouse.api.domain.outbound

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface OutboundWaveRepository : JpaRepository<OutboundWaveEntity, Long>, JpaSpecificationExecutor<OutboundWaveEntity> {
    fun existsByWaveNo(waveNo: String): Boolean
}

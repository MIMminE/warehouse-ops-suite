package dev.portfolio.warehouse.api.domain.outbound

import org.springframework.data.jpa.repository.JpaRepository

interface OutboundWaveRepository : JpaRepository<OutboundWaveEntity, Long> {
    fun existsByWaveNo(waveNo: String): Boolean
}


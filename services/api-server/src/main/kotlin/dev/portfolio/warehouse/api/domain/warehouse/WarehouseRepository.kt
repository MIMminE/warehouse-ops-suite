package dev.portfolio.warehouse.api.domain.warehouse

import org.springframework.data.jpa.repository.JpaRepository

interface WarehouseRepository : JpaRepository<WarehouseEntity, Long> {
    fun existsByCode(code: String): Boolean
}


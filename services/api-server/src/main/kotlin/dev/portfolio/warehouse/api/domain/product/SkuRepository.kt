package dev.portfolio.warehouse.api.domain.product

import org.springframework.data.jpa.repository.JpaRepository

interface SkuRepository : JpaRepository<SkuEntity, Long> {
    fun existsByCode(code: String): Boolean
}


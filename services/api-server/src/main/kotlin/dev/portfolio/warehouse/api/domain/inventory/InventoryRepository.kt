package dev.portfolio.warehouse.api.domain.inventory

import org.springframework.data.jpa.repository.JpaRepository

interface InventoryRepository : JpaRepository<InventoryEntity, Long>


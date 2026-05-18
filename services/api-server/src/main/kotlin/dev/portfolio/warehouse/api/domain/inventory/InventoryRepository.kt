package dev.portfolio.warehouse.api.domain.inventory

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface InventoryRepository : JpaRepository<InventoryEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select i
        from InventoryEntity i
        where i.clientCompany.id = :clientCompanyId
          and i.warehouse.id = :warehouseId
          and i.sku.id = :skuId
          and i.availableQuantity > 0
        order by i.id asc
        """,
    )
    fun findAllocatableInventories(
        @Param("clientCompanyId") clientCompanyId: Long,
        @Param("warehouseId") warehouseId: Long,
        @Param("skuId") skuId: Long,
    ): List<InventoryEntity>
}

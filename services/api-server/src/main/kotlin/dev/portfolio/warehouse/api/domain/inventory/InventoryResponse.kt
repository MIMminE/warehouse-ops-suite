package dev.portfolio.warehouse.api.domain.inventory

import java.time.LocalDateTime

data class InventoryResponse(
    val id: Long,
    val clientCompanyId: Long,
    val clientCompanyName: String,
    val warehouseId: Long,
    val warehouseName: String,
    val locationId: Long,
    val locationCode: String,
    val locationZone: String?,
    val skuId: Long,
    val skuCode: String,
    val skuName: String,
    val availableQuantity: Int,
    val allocatedQuantity: Int,
    val holdQuantity: Int,
    val status: String,
    val updatedAt: LocalDateTime,
)

fun InventoryEntity.toResponse(): InventoryResponse =
    InventoryResponse(
        id = requireNotNull(id),
        clientCompanyId = requireNotNull(clientCompany.id),
        clientCompanyName = clientCompany.name,
        warehouseId = requireNotNull(warehouse.id),
        warehouseName = warehouse.name,
        locationId = requireNotNull(location.id),
        locationCode = location.code,
        locationZone = location.zone,
        skuId = requireNotNull(sku.id),
        skuCode = sku.code,
        skuName = sku.name,
        availableQuantity = availableQuantity,
        allocatedQuantity = allocatedQuantity,
        holdQuantity = 0,
        status = when {
            availableQuantity == 0 -> "부족주의"
            allocatedQuantity > availableQuantity -> "부족주의"
            else -> "정상"
        },
        updatedAt = updatedAt,
    )

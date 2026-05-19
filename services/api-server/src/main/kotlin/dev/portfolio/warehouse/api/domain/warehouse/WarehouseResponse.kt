package dev.portfolio.warehouse.api.domain.warehouse

data class WarehouseResponse(
    val id: Long,
    val code: String,
    val name: String,
)

fun WarehouseEntity.toResponse(): WarehouseResponse =
    WarehouseResponse(
        id = requireNotNull(id),
        code = code,
        name = name,
    )

package dev.portfolio.warehouse.api.domain.warehouse

import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/warehouses")
class WarehouseController(
    private val warehouseRepository: WarehouseRepository,
) {
    @GetMapping
    fun search(): List<WarehouseResponse> =
        warehouseRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
            .map { it.toResponse() }
}

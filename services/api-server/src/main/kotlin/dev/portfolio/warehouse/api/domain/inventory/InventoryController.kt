package dev.portfolio.warehouse.api.domain.inventory

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/inventories")
class InventoryController(
    private val inventoryQueryService: InventoryQueryService,
) {
    @GetMapping
    fun search(
        @RequestParam(required = false) clientCompanyId: Long?,
        @RequestParam(required = false) warehouseId: Long?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) keyword: String?,
    ): List<InventoryResponse> =
        inventoryQueryService.search(
            clientCompanyId = clientCompanyId,
            warehouseId = warehouseId,
            status = status,
            keyword = keyword,
        )
}

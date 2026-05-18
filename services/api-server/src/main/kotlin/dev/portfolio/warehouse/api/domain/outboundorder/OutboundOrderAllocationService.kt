package dev.portfolio.warehouse.api.domain.outboundorder

import dev.portfolio.warehouse.api.domain.inventory.InventoryRepository
import dev.portfolio.warehouse.api.support.error.BadRequestException
import dev.portfolio.warehouse.api.support.error.InsufficientStockException
import dev.portfolio.warehouse.api.support.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutboundOrderAllocationService(
    private val outboundOrderRepository: OutboundOrderRepository,
    private val outboundOrderLineRepository: OutboundOrderLineRepository,
    private val inventoryRepository: InventoryRepository,
) {
    @Transactional
    fun allocate(orderId: Long): OutboundOrderDetailResponse {
        val order = outboundOrderRepository.findById(orderId)
            .orElseThrow { NotFoundException("출고 지시를 찾을 수 없습니다: $orderId") }
        if (order.status == OutboundOrderStatus.CANCELED || order.status == OutboundOrderStatus.SHIPPED) {
            throw BadRequestException("할당할 수 없는 출고 지시 상태입니다: ${order.status}")
        }

        val lines = outboundOrderLineRepository.findByOutboundOrderIdOrderByLineNo(orderId)
        if (lines.isEmpty()) {
            throw BadRequestException("출고 지시 라인이 없습니다.")
        }

        lines.forEach { line ->
            val remainingQuantity = line.orderedQuantity - line.allocatedQuantity
            if (remainingQuantity <= 0) {
                return@forEach
            }

            val inventories = inventoryRepository.findAllocatableInventories(
                clientCompanyId = requireNotNull(order.clientCompany.id),
                warehouseId = requireNotNull(order.warehouse.id),
                skuId = requireNotNull(line.sku.id),
            )
            val availableQuantity = inventories.sumOf { it.availableQuantity }
            if (availableQuantity < remainingQuantity) {
                throw InsufficientStockException(
                    "SKU ${line.sku.code} 재고가 부족합니다. 필요 수량: $remainingQuantity, 가용 수량: $availableQuantity",
                )
            }

            var quantityToAllocate = remainingQuantity
            inventories.forEach { inventory ->
                if (quantityToAllocate <= 0) {
                    return@forEach
                }
                val allocated = minOf(inventory.availableQuantity, quantityToAllocate)
                inventory.availableQuantity -= allocated
                inventory.allocatedQuantity += allocated
                quantityToAllocate -= allocated
            }
            line.allocatedQuantity += remainingQuantity
        }

        order.status = OutboundOrderStatus.ALLOCATED

        return OutboundOrderDetailResponse(
            order = order.toResponse(),
            lines = lines.map { it.toResponse() },
        )
    }
}


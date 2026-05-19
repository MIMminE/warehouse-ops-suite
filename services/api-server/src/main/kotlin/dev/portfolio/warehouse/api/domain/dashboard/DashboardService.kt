package dev.portfolio.warehouse.api.domain.dashboard

import dev.portfolio.warehouse.api.domain.client.ClientCompanyRepository
import dev.portfolio.warehouse.api.domain.inventory.InventoryRepository
import dev.portfolio.warehouse.api.domain.outbound.OutboundWaveRepository
import dev.portfolio.warehouse.api.domain.outbound.OutboundWaveStatus
import dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderLineRepository
import dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderRepository
import dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderStatus
import dev.portfolio.warehouse.api.domain.picking.PickingTaskRepository
import dev.portfolio.warehouse.api.domain.receiving.ReceivingOrderLineRepository
import dev.portfolio.warehouse.api.domain.receiving.ReceivingOrderRepository
import dev.portfolio.warehouse.api.domain.receiving.ReceivingOrderStatus
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DashboardService(
    private val clientCompanyRepository: ClientCompanyRepository,
    private val receivingOrderRepository: ReceivingOrderRepository,
    private val receivingOrderLineRepository: ReceivingOrderLineRepository,
    private val inventoryRepository: InventoryRepository,
    private val outboundOrderRepository: OutboundOrderRepository,
    private val outboundOrderLineRepository: OutboundOrderLineRepository,
    private val outboundWaveRepository: OutboundWaveRepository,
    private val pickingTaskRepository: PickingTaskRepository,
) {
    @Transactional(readOnly = true)
    fun getDashboard(): DashboardResponse {
        val receivingOrders = receivingOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
        val receivingLinesByOrderId = receivingOrders.associate { order ->
            val orderId = requireNotNull(order.id)
            orderId to receivingOrderLineRepository.findByReceivingOrderIdOrderByLineNo(orderId)
        }
        val inventories = inventoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
        val outboundOrders = outboundOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
        val outboundLinesByOrderId = outboundOrders.associate { order ->
            val orderId = requireNotNull(order.id)
            orderId to outboundOrderLineRepository.findByOutboundOrderIdOrderByLineNo(orderId)
        }
        val pickingTasks = pickingTaskRepository.findAll()
        val outboundWaves = outboundWaveRepository.findAll()

        val receivingRequestedQuantity = receivingLinesByOrderId.values.flatten().sumOf { it.requestedQuantity }
        val inventoryAvailableQuantity = inventories.sumOf { it.availableQuantity }
        val inventoryAllocatedQuantity = inventories.sumOf { it.allocatedQuantity }
        val pickingTaskCount = pickingTasks.sumOf { it.requestedQuantity }
        val pickingPickedQuantity = pickingTasks.sumOf { it.pickedQuantity }
        val outboundNeedsAttentionCount = outboundOrders.count {
            it.status == OutboundOrderStatus.RECEIVED || it.status == OutboundOrderStatus.CANCELED
        }

        return DashboardResponse(
            metrics = DashboardMetricsResponse(
                receivingOrderCount = receivingOrders.size,
                receivingRequestedQuantity = receivingRequestedQuantity,
                inventoryAvailableQuantity = inventoryAvailableQuantity,
                inventoryAllocatedQuantity = inventoryAllocatedQuantity,
                inventoryHoldQuantity = 0,
                outboundOrderCount = outboundOrders.size,
                outboundNeedsAttentionCount = outboundNeedsAttentionCount,
                pickingTaskCount = pickingTaskCount,
                pickingPickedQuantity = pickingPickedQuantity,
            ),
            issueQueue = listOf(
                DashboardIssueResponse(
                    label = "입고 검수 대기",
                    count = receivingOrders.count { it.status == ReceivingOrderStatus.RECEIVING },
                    description = "검수 수량 반영 후 적치 작업 생성",
                ),
                DashboardIssueResponse(
                    label = "출고 할당 확인",
                    count = outboundOrders.count { it.status == OutboundOrderStatus.RECEIVED },
                    description = "가용 재고와 주문 수량 확인",
                ),
                DashboardIssueResponse(
                    label = "DPS 웨이브 대기",
                    count = outboundWaves.count { it.status == OutboundWaveStatus.READY },
                    description = "DPS Agent 연결 후 작업 시작",
                ),
                DashboardIssueResponse(
                    label = "할당 재고",
                    count = inventories.count { it.allocatedQuantity > 0 },
                    description = "피킹 전 할당 수량 확인",
                ),
            ),
            clientSla = clientSla(outboundOrders, outboundLinesByOrderId),
            recentReceiving = receivingOrders.take(4).map { order ->
                DashboardListItemResponse(
                    label = order.receivingNo,
                    description = "${order.clientCompany.name} / ${order.warehouse.name} / ${order.requestedBy}",
                    status = receivingStatusLabel(order.status),
                )
            },
            recentOutbound = outboundOrders.take(4).map { order ->
                DashboardListItemResponse(
                    label = order.outboundOrderNo,
                    description = "${order.clientCompany.name} / ${order.receiverName} / ${order.intakeSource}",
                    status = outboundStatusLabel(order.status),
                )
            },
            inventoryAlerts = inventories
                .filter { it.allocatedQuantity > 0 || it.availableQuantity == 0 }
                .take(4)
                .map { inventory ->
                    DashboardListItemResponse(
                        label = inventory.sku.code,
                        description = "${inventory.clientCompany.name} / ${inventory.location.code} / 할당 ${inventory.allocatedQuantity}",
                        status = if (inventory.availableQuantity == 0) "부족주의" else "할당중",
                    )
                },
        )
    }

    private fun clientSla(
        outboundOrders: List<dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderEntity>,
        outboundLinesByOrderId: Map<Long, List<dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderLineEntity>>,
    ): List<ClientSlaResponse> =
        clientCompanyRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).map { client ->
            val clientOrders = outboundOrders.filter { it.clientCompany.id == client.id }
            val total = clientOrders.size
            val completed = clientOrders.count { order ->
                val lines = outboundLinesByOrderId[requireNotNull(order.id)].orEmpty()
                lines.isNotEmpty() && lines.all { it.pickedQuantity >= it.orderedQuantity }
            }

            ClientSlaResponse(
                clientCompanyName = client.name,
                rate = if (total == 0) 0 else (completed * 100) / total,
            )
        }
}

private fun receivingStatusLabel(status: ReceivingOrderStatus): String =
    when (status) {
        ReceivingOrderStatus.DRAFT -> "초안"
        ReceivingOrderStatus.REQUESTED -> "입고예정"
        ReceivingOrderStatus.RECEIVING -> "검수중"
        ReceivingOrderStatus.PUTAWAY -> "적치중"
        ReceivingOrderStatus.COMPLETED -> "적치완료"
        ReceivingOrderStatus.CANCELED -> "취소"
    }

private fun outboundStatusLabel(status: OutboundOrderStatus): String =
    when (status) {
        OutboundOrderStatus.RECEIVED -> "지시접수"
        OutboundOrderStatus.ALLOCATED -> "할당완료"
        OutboundOrderStatus.WAVE_ASSIGNED -> "웨이브할당"
        OutboundOrderStatus.PICKING -> "피킹중"
        OutboundOrderStatus.PACKING -> "패킹중"
        OutboundOrderStatus.READY_TO_SHIP -> "피킹완료"
        OutboundOrderStatus.SHIPPED -> "출고완료"
        OutboundOrderStatus.CANCELED -> "취소"
    }

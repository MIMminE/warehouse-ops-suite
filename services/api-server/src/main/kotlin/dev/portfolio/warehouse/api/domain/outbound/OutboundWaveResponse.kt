package dev.portfolio.warehouse.api.domain.outbound

import dev.portfolio.warehouse.api.domain.picking.PickingTaskEntity
import dev.portfolio.warehouse.api.domain.picking.PickingTaskStatus

data class OutboundWaveResponse(
    val id: Long,
    val waveNo: String,
    val clientCompanyId: Long,
    val clientCompanyName: String,
    val warehouseId: Long,
    val warehouseName: String,
    val status: OutboundWaveStatus,
    val requestedBy: String,
    val createdAt: String,
    val orderCount: Int,
    val taskCount: Int,
    val requestedQuantity: Int,
    val pickedQuantity: Int,
    val pickingTasks: List<OutboundWavePickingTaskResponse>,
)

data class OutboundWavePickingTaskResponse(
    val id: Long,
    val taskNo: String,
    val outboundOrderLineId: Long?,
    val outboundOrderNo: String?,
    val receiverName: String?,
    val sourceLocationId: Long,
    val sourceLocationCode: String,
    val skuId: Long,
    val skuCode: String,
    val skuName: String,
    val status: PickingTaskStatus,
    val requestedQuantity: Int,
    val pickedQuantity: Int,
    val assignedWorker: String?,
)

fun OutboundWaveEntity.toResponse(tasks: List<PickingTaskEntity>): OutboundWaveResponse =
    OutboundWaveResponse(
        id = requireNotNull(id),
        waveNo = waveNo,
        clientCompanyId = requireNotNull(clientCompany.id),
        clientCompanyName = clientCompany.name,
        warehouseId = requireNotNull(warehouse.id),
        warehouseName = warehouse.name,
        status = status,
        requestedBy = requestedBy,
        createdAt = createdAt.toLocalDate().toString(),
        orderCount = tasks.mapNotNull { it.outboundOrderLine?.outboundOrder?.id }.distinct().size,
        taskCount = tasks.size,
        requestedQuantity = tasks.sumOf { it.requestedQuantity },
        pickedQuantity = tasks.sumOf { it.pickedQuantity },
        pickingTasks = tasks.map { it.toWaveResponse() },
    )

fun PickingTaskEntity.toWaveResponse(): OutboundWavePickingTaskResponse =
    OutboundWavePickingTaskResponse(
        id = requireNotNull(id),
        taskNo = taskNo,
        outboundOrderLineId = outboundOrderLine?.id,
        outboundOrderNo = outboundOrderLine?.outboundOrder?.outboundOrderNo,
        receiverName = outboundOrderLine?.outboundOrder?.receiverName,
        sourceLocationId = requireNotNull(sourceLocation.id),
        sourceLocationCode = sourceLocation.code,
        skuId = requireNotNull(sku.id),
        skuCode = sku.code,
        skuName = sku.name,
        status = status,
        requestedQuantity = requestedQuantity,
        pickedQuantity = pickedQuantity,
        assignedWorker = assignedWorker,
    )

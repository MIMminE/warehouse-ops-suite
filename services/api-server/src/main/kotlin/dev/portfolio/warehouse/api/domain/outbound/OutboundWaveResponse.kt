package dev.portfolio.warehouse.api.domain.outbound

import dev.portfolio.warehouse.api.domain.picking.PickingTaskEntity
import dev.portfolio.warehouse.api.domain.picking.PickingTaskStatus

data class OutboundWaveResponse(
    val id: Long,
    val waveNo: String,
    val clientCompanyId: Long,
    val warehouseId: Long,
    val status: OutboundWaveStatus,
    val pickingTasks: List<OutboundWavePickingTaskResponse>,
)

data class OutboundWavePickingTaskResponse(
    val id: Long,
    val taskNo: String,
    val outboundOrderLineId: Long?,
    val sourceLocationId: Long,
    val skuId: Long,
    val status: PickingTaskStatus,
    val requestedQuantity: Int,
    val pickedQuantity: Int,
)

fun OutboundWaveEntity.toResponse(tasks: List<PickingTaskEntity>): OutboundWaveResponse =
    OutboundWaveResponse(
        id = requireNotNull(id),
        waveNo = waveNo,
        clientCompanyId = requireNotNull(clientCompany.id),
        warehouseId = requireNotNull(warehouse.id),
        status = status,
        pickingTasks = tasks.map { it.toWaveResponse() },
    )

fun PickingTaskEntity.toWaveResponse(): OutboundWavePickingTaskResponse =
    OutboundWavePickingTaskResponse(
        id = requireNotNull(id),
        taskNo = taskNo,
        outboundOrderLineId = outboundOrderLine?.id,
        sourceLocationId = requireNotNull(sourceLocation.id),
        skuId = requireNotNull(sku.id),
        status = status,
        requestedQuantity = requestedQuantity,
        pickedQuantity = pickedQuantity,
    )


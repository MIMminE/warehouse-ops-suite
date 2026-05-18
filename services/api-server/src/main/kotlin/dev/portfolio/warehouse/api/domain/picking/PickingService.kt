package dev.portfolio.warehouse.api.domain.picking

import dev.portfolio.warehouse.api.domain.outbound.OutboundWaveStatus
import dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderStatus
import dev.portfolio.warehouse.api.support.error.BadRequestException
import dev.portfolio.warehouse.api.support.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PickingService(
    private val pickingTaskRepository: PickingTaskRepository,
) {
    @Transactional
    fun confirm(taskId: Long, pickedQuantity: Int): PickingTaskResponse {
        val task = pickingTaskRepository.findById(taskId)
            .orElseThrow { NotFoundException("피킹 작업을 찾을 수 없습니다: $taskId") }
        if (task.status == PickingTaskStatus.CANCELED || task.status == PickingTaskStatus.COMPLETED) {
            throw BadRequestException("피킹 완료 처리할 수 없는 작업 상태입니다: ${task.status}")
        }
        val remainingQuantity = task.requestedQuantity - task.pickedQuantity
        if (pickedQuantity > remainingQuantity) {
            throw BadRequestException("피킹 수량이 남은 작업 수량을 초과했습니다.")
        }

        task.pickedQuantity += pickedQuantity
        task.status = if (task.pickedQuantity == task.requestedQuantity) {
            PickingTaskStatus.COMPLETED
        } else {
            PickingTaskStatus.PICKING
        }

        task.outboundOrderLine?.let { line ->
            line.pickedQuantity += pickedQuantity
            line.outboundOrder.status = OutboundOrderStatus.PICKING
        }
        task.outboundOrderLineAllocation?.let { allocation ->
            allocation.pickedQuantity += pickedQuantity
        }

        val waveTasks = pickingTaskRepository.findByOutboundWaveIdOrderById(requireNotNull(task.outboundWave.id))
        task.outboundWave.status = if (waveTasks.all { it.status == PickingTaskStatus.COMPLETED }) {
            OutboundWaveStatus.COMPLETED
        } else {
            OutboundWaveStatus.PICKING
        }

        return task.toResponse()
    }
}


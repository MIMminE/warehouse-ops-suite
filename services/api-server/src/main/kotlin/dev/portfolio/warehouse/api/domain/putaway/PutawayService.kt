package dev.portfolio.warehouse.api.domain.putaway

import dev.portfolio.warehouse.api.domain.inventory.InventoryEntity
import dev.portfolio.warehouse.api.domain.inventory.InventoryRepository
import dev.portfolio.warehouse.api.domain.receiving.ReceivingOrderLineRepository
import dev.portfolio.warehouse.api.domain.receiving.ReceivingOrderStatus
import dev.portfolio.warehouse.api.domain.warehouse.LocationRepository
import dev.portfolio.warehouse.api.support.error.BadRequestException
import dev.portfolio.warehouse.api.support.error.DuplicateResourceException
import dev.portfolio.warehouse.api.support.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PutawayService(
    private val receivingOrderLineRepository: ReceivingOrderLineRepository,
    private val locationRepository: LocationRepository,
    private val putawayTaskRepository: PutawayTaskRepository,
    private val inventoryRepository: InventoryRepository,
) {
    @Transactional
    fun create(request: CreatePutawayTaskRequest): PutawayTaskResponse {
        if (putawayTaskRepository.existsByTaskNo(request.taskNo)) {
            throw DuplicateResourceException("이미 존재하는 적치 작업 번호입니다: ${request.taskNo}")
        }
        val line = receivingOrderLineRepository.findById(request.receivingOrderLineId)
            .orElseThrow { NotFoundException("입고 상세를 찾을 수 없습니다: ${request.receivingOrderLineId}") }
        val targetLocation = locationRepository.findById(request.targetLocationId)
            .orElseThrow { NotFoundException("대상 로케이션을 찾을 수 없습니다: ${request.targetLocationId}") }
        if (targetLocation.warehouse.id != line.receivingOrder.warehouse.id) {
            throw BadRequestException("입고 창고와 다른 창고의 로케이션에는 적치할 수 없습니다.")
        }

        val existingPutawayQuantity = putawayTaskRepository.findByReceivingOrderLineId(request.receivingOrderLineId)
            .filter { it.status != PutawayTaskStatus.CANCELED }
            .sumOf { it.putawayQuantity }
        if (existingPutawayQuantity + request.putawayQuantity > line.receivedQuantity) {
            throw BadRequestException("적치 작업 수량이 입고 완료 수량을 초과했습니다.")
        }

        val task = putawayTaskRepository.save(
            PutawayTaskEntity(
                taskNo = request.taskNo,
                receivingOrderLine = line,
                warehouse = line.receivingOrder.warehouse,
                targetLocation = targetLocation,
                sku = line.sku,
                status = PutawayTaskStatus.READY,
                putawayQuantity = request.putawayQuantity,
                assignedWorker = request.assignedWorker,
            ),
        )
        line.receivingOrder.status = ReceivingOrderStatus.PUTAWAY
        return task.toResponse()
    }

    @Transactional
    fun complete(taskId: Long): PutawayTaskResponse {
        val task = putawayTaskRepository.findById(taskId)
            .orElseThrow { NotFoundException("적치 작업을 찾을 수 없습니다: $taskId") }
        if (task.status == PutawayTaskStatus.CANCELED || task.status == PutawayTaskStatus.COMPLETED) {
            throw BadRequestException("완료 처리할 수 없는 적치 작업 상태입니다: ${task.status}")
        }

        val clientCompany = task.receivingOrderLine.receivingOrder.clientCompany
        val inventory = inventoryRepository.findByClientCompanyIdAndLocationIdAndSkuId(
            clientCompanyId = requireNotNull(clientCompany.id),
            locationId = requireNotNull(task.targetLocation.id),
            skuId = requireNotNull(task.sku.id),
        ) ?: InventoryEntity(
            clientCompany = clientCompany,
            warehouse = task.warehouse,
            location = task.targetLocation,
            sku = task.sku,
            availableQuantity = 0,
        )

        inventory.availableQuantity += task.putawayQuantity
        inventoryRepository.save(inventory)

        task.status = PutawayTaskStatus.COMPLETED
        return task.toResponse()
    }
}


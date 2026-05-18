package dev.portfolio.warehouse.api.domain.outbound

import dev.portfolio.warehouse.api.domain.client.ClientCompanyRepository
import dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderLineAllocationRepository
import dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderLineRepository
import dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderStatus
import dev.portfolio.warehouse.api.domain.picking.PickingTaskEntity
import dev.portfolio.warehouse.api.domain.picking.PickingTaskRepository
import dev.portfolio.warehouse.api.domain.warehouse.WarehouseRepository
import dev.portfolio.warehouse.api.support.error.BadRequestException
import dev.portfolio.warehouse.api.support.error.DuplicateResourceException
import dev.portfolio.warehouse.api.support.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutboundWaveService(
    private val clientCompanyRepository: ClientCompanyRepository,
    private val warehouseRepository: WarehouseRepository,
    private val outboundWaveRepository: OutboundWaveRepository,
    private val outboundOrderLineRepository: OutboundOrderLineRepository,
    private val outboundOrderLineAllocationRepository: OutboundOrderLineAllocationRepository,
    private val pickingTaskRepository: PickingTaskRepository,
) {
    @Transactional
    fun create(request: CreateOutboundWaveRequest): OutboundWaveResponse {
        if (outboundWaveRepository.existsByWaveNo(request.waveNo)) {
            throw DuplicateResourceException("이미 존재하는 출고 웨이브 번호입니다: ${request.waveNo}")
        }
        if (request.outboundOrderLineIds.isEmpty()) {
            throw BadRequestException("웨이브에 포함할 출고 지시 라인이 필요합니다.")
        }

        val clientCompany = clientCompanyRepository.findById(request.clientCompanyId)
            .orElseThrow { NotFoundException("고객사를 찾을 수 없습니다: ${request.clientCompanyId}") }
        val warehouse = warehouseRepository.findById(request.warehouseId)
            .orElseThrow { NotFoundException("창고를 찾을 수 없습니다: ${request.warehouseId}") }
        val lines = outboundOrderLineRepository.findAllById(request.outboundOrderLineIds)
        if (lines.size != request.outboundOrderLineIds.toSet().size) {
            throw NotFoundException("일부 출고 지시 라인을 찾을 수 없습니다.")
        }

        lines.forEach { line ->
            if (line.outboundOrder.clientCompany.id != clientCompany.id || line.outboundOrder.warehouse.id != warehouse.id) {
                throw BadRequestException("웨이브의 고객사 또는 창고와 맞지 않는 출고 지시 라인이 포함되어 있습니다.")
            }
            if (line.allocatedQuantity <= line.pickedQuantity) {
                throw BadRequestException("피킹 가능한 할당 수량이 없는 출고 지시 라인입니다: ${line.id}")
            }
        }

        val wave = outboundWaveRepository.save(
            OutboundWaveEntity(
                waveNo = request.waveNo,
                clientCompany = clientCompany,
                warehouse = warehouse,
                status = OutboundWaveStatus.ALLOCATED,
                requestedBy = request.requestedBy,
                memo = request.memo,
            ),
        )

        val allocations = outboundOrderLineAllocationRepository.findByOutboundOrderLineIds(request.outboundOrderLineIds)
        if (allocations.isEmpty()) {
            throw BadRequestException("웨이브 생성에 필요한 재고 할당 상세가 없습니다.")
        }

        val tasks = allocations.mapIndexed { index, allocation ->
            val line = allocation.outboundOrderLine
            line.outboundOrder.status = OutboundOrderStatus.WAVE_ASSIGNED
            PickingTaskEntity(
                taskNo = "${request.waveNo}-${index + 1}",
                outboundWave = wave,
                outboundOrderLine = line,
                warehouse = warehouse,
                sourceLocation = allocation.location,
                sku = allocation.sku,
                requestedQuantity = allocation.allocatedQuantity - allocation.pickedQuantity,
            )
        }
        pickingTaskRepository.saveAll(tasks)

        return wave.toResponse(tasks)
    }
}


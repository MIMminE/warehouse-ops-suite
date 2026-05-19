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
import jakarta.persistence.criteria.JoinType
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class OutboundWaveService(
    private val clientCompanyRepository: ClientCompanyRepository,
    private val warehouseRepository: WarehouseRepository,
    private val outboundWaveRepository: OutboundWaveRepository,
    private val outboundOrderLineRepository: OutboundOrderLineRepository,
    private val outboundOrderLineAllocationRepository: OutboundOrderLineAllocationRepository,
    private val pickingTaskRepository: PickingTaskRepository,
) {
    @Transactional(readOnly = true)
    fun getCandidates(
        clientCompanyId: Long?,
        warehouseId: Long?,
    ): List<OutboundWaveCandidateLineResponse> =
        outboundOrderLineRepository.findAll()
            .asSequence()
            .filter { it.allocatedQuantity > it.pickedQuantity }
            .filter { clientCompanyId == null || it.outboundOrder.clientCompany.id == clientCompanyId }
            .filter { warehouseId == null || it.outboundOrder.warehouse.id == warehouseId }
            .filter { !pickingTaskRepository.existsByOutboundOrderLineId(requireNotNull(it.id)) }
            .sortedWith(
                compareBy(
                    { it.outboundOrder.requestedShipDate },
                    { it.outboundOrder.id },
                    { it.lineNo },
                ),
            )
            .map { it.toWaveCandidateResponse() }
            .toList()

    @Transactional(readOnly = true)
    fun search(
        clientCompanyId: Long?,
        warehouseId: Long?,
        status: OutboundWaveStatus?,
        createdFrom: LocalDate?,
        createdTo: LocalDate?,
    ): List<OutboundWaveResponse> {
        val waves = outboundWaveRepository.findAll(
            outboundWaveSearchSpec(
                clientCompanyId = clientCompanyId,
                warehouseId = warehouseId,
                status = status,
                createdFrom = createdFrom,
                createdTo = createdTo,
            ),
            Sort.by(Sort.Direction.DESC, "id"),
        )

        return waves.map { wave ->
            val tasks = pickingTaskRepository.findByOutboundWaveIdOrderById(requireNotNull(wave.id))
            wave.toResponse(tasks)
        }
    }

    @Transactional(readOnly = true)
    fun getDetail(waveId: Long): OutboundWaveResponse {
        val wave = outboundWaveRepository.findById(waveId)
            .orElseThrow { NotFoundException("출고 웨이브를 찾을 수 없습니다: $waveId") }
        val tasks = pickingTaskRepository.findByOutboundWaveIdOrderById(waveId)
        return wave.toResponse(tasks)
    }

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
                outboundOrderLineAllocation = allocation,
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

private fun outboundWaveSearchSpec(
    clientCompanyId: Long?,
    warehouseId: Long?,
    status: OutboundWaveStatus?,
    createdFrom: LocalDate?,
    createdTo: LocalDate?,
): Specification<OutboundWaveEntity> =
    Specification { root, _, criteriaBuilder ->
        root.fetch<Any, Any>("clientCompany", JoinType.LEFT)
        root.fetch<Any, Any>("warehouse", JoinType.LEFT)

        val predicates = listOfNotNull(
            clientCompanyId?.let {
                criteriaBuilder.equal(root.get<Any>("clientCompany").get<Long>("id"), it)
            },
            warehouseId?.let {
                criteriaBuilder.equal(root.get<Any>("warehouse").get<Long>("id"), it)
            },
            status?.let {
                criteriaBuilder.equal(root.get<OutboundWaveStatus>("status"), it)
            },
            createdFrom?.let {
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), it.atStartOfDay())
            },
            createdTo?.let {
                criteriaBuilder.lessThan(root.get("createdAt"), it.plusDays(1).atStartOfDay())
            },
        )

        criteriaBuilder.and(*predicates.toTypedArray())
    }

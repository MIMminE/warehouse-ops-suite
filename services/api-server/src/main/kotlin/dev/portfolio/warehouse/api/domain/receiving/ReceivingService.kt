package dev.portfolio.warehouse.api.domain.receiving

import dev.portfolio.warehouse.api.domain.client.ClientCompanyRepository
import dev.portfolio.warehouse.api.domain.product.SkuRepository
import dev.portfolio.warehouse.api.domain.warehouse.WarehouseRepository
import dev.portfolio.warehouse.api.support.error.BadRequestException
import dev.portfolio.warehouse.api.support.error.DuplicateResourceException
import dev.portfolio.warehouse.api.support.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReceivingService(
    private val clientCompanyRepository: ClientCompanyRepository,
    private val warehouseRepository: WarehouseRepository,
    private val skuRepository: SkuRepository,
    private val receivingOrderRepository: ReceivingOrderRepository,
    private val receivingOrderLineRepository: ReceivingOrderLineRepository,
) {
    @Transactional
    fun create(request: CreateReceivingOrderRequest): ReceivingOrderResponse {
        if (receivingOrderRepository.existsByReceivingNo(request.receivingNo)) {
            throw DuplicateResourceException("이미 존재하는 입고 번호입니다: ${request.receivingNo}")
        }
        val clientCompany = clientCompanyRepository.findById(request.clientCompanyId)
            .orElseThrow { NotFoundException("고객사를 찾을 수 없습니다: ${request.clientCompanyId}") }
        val warehouse = warehouseRepository.findById(request.warehouseId)
            .orElseThrow { NotFoundException("창고를 찾을 수 없습니다: ${request.warehouseId}") }

        val order = receivingOrderRepository.save(
            ReceivingOrderEntity(
                receivingNo = request.receivingNo,
                clientCompany = clientCompany,
                warehouse = warehouse,
                status = ReceivingOrderStatus.REQUESTED,
                supplierName = request.supplierName,
                requestedBy = request.requestedBy,
                memo = request.memo,
            ),
        )
        val lines = request.lines.map { line ->
            val sku = skuRepository.findById(line.skuId)
                .orElseThrow { NotFoundException("SKU를 찾을 수 없습니다: ${line.skuId}") }
            if (sku.clientCompany.id != clientCompany.id) {
                throw BadRequestException("입고 라인의 SKU가 고객사 소유 상품이 아닙니다: ${line.skuId}")
            }
            ReceivingOrderLineEntity(
                receivingOrder = order,
                lineNo = line.lineNo,
                sku = sku,
                requestedQuantity = line.requestedQuantity,
            )
        }
        receivingOrderLineRepository.saveAll(lines)
        return order.toResponse(lines)
    }

    @Transactional
    fun receiveLine(lineId: Long, receivedQuantity: Int): ReceivingOrderLineResponse {
        val line = receivingOrderLineRepository.findById(lineId)
            .orElseThrow { NotFoundException("입고 상세를 찾을 수 없습니다: $lineId") }
        if (line.receivedQuantity + receivedQuantity > line.requestedQuantity) {
            throw BadRequestException("입고 수량이 요청 수량을 초과했습니다.")
        }
        line.receivedQuantity += receivedQuantity
        line.receivingOrder.status = ReceivingOrderStatus.RECEIVING
        return line.toResponse()
    }
}


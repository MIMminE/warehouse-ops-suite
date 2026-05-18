package dev.portfolio.warehouse.api.domain.outboundorder

import dev.portfolio.warehouse.api.domain.client.ClientCompanyRepository
import dev.portfolio.warehouse.api.domain.product.SkuRepository
import dev.portfolio.warehouse.api.domain.warehouse.WarehouseRepository
import dev.portfolio.warehouse.api.support.error.BadRequestException
import dev.portfolio.warehouse.api.support.error.DuplicateResourceException
import dev.portfolio.warehouse.api.support.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutboundOrderIntakeService(
    private val clientCompanyRepository: ClientCompanyRepository,
    private val warehouseRepository: WarehouseRepository,
    private val skuRepository: SkuRepository,
    private val outboundOrderRepository: OutboundOrderRepository,
    private val outboundOrderLineRepository: OutboundOrderLineRepository,
) : OutboundOrderIntakePort {
    override val source: OutboundOrderIntakeSource = OutboundOrderIntakeSource.API

    @Transactional
    override fun receive(command: OutboundOrderIntakeCommand): OutboundOrderEntity {
        if (command.lines.isEmpty()) {
            throw BadRequestException("출고 지시 라인은 1개 이상이어야 합니다.")
        }
        if (outboundOrderRepository.existsByClientCompanyIdAndOutboundOrderNo(command.clientCompanyId, command.outboundOrderNo)) {
            throw DuplicateResourceException("이미 접수된 출고 지시 번호입니다: ${command.outboundOrderNo}")
        }

        val clientCompany = clientCompanyRepository.findById(command.clientCompanyId)
            .orElseThrow { NotFoundException("고객사를 찾을 수 없습니다: ${command.clientCompanyId}") }
        val warehouse = warehouseRepository.findById(command.warehouseId)
            .orElseThrow { NotFoundException("창고를 찾을 수 없습니다: ${command.warehouseId}") }

        val order = outboundOrderRepository.save(
            OutboundOrderEntity(
                clientCompany = clientCompany,
                warehouse = warehouse,
                outboundOrderNo = command.outboundOrderNo,
                externalReferenceNo = command.externalReferenceNo,
                intakeSource = source,
                receiverName = command.receiver.name,
                receiverPhone = command.receiver.phone,
                zipCode = command.receiver.zipCode,
                address1 = command.receiver.address1,
                address2 = command.receiver.address2,
                deliveryMemo = command.receiver.deliveryMemo,
                requestedShipDate = command.requestedShipDate,
                orderedAt = command.orderedAt,
            ),
        )

        val lines = command.lines.map { line ->
            val sku = skuRepository.findById(line.skuId)
                .orElseThrow { NotFoundException("SKU를 찾을 수 없습니다: ${line.skuId}") }
            if (sku.clientCompany.id != clientCompany.id) {
                throw BadRequestException("출고 지시 라인의 SKU가 고객사 소유 상품이 아닙니다: ${line.skuId}")
            }
            if (line.orderedQuantity <= 0) {
                throw BadRequestException("출고 수량은 1 이상이어야 합니다.")
            }
            OutboundOrderLineEntity(
                outboundOrder = order,
                lineNo = line.lineNo,
                sku = sku,
                orderedQuantity = line.orderedQuantity,
            )
        }
        outboundOrderLineRepository.saveAll(lines)

        return order
    }
}


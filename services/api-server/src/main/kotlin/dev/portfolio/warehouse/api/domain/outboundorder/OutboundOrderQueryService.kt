package dev.portfolio.warehouse.api.domain.outboundorder

import dev.portfolio.warehouse.api.support.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class OutboundOrderQueryService(
    private val outboundOrderRepository: OutboundOrderRepository,
    private val outboundOrderLineRepository: OutboundOrderLineRepository,
) {
    @Transactional(readOnly = true)
    fun search(
        clientCompanyId: Long?,
        status: OutboundOrderStatus?,
        requestedShipDateFrom: LocalDate?,
        requestedShipDateTo: LocalDate?,
    ): List<OutboundOrderResponse> =
        outboundOrderRepository.search(
            clientCompanyId = clientCompanyId,
            status = status,
            requestedShipDateFrom = requestedShipDateFrom,
            requestedShipDateTo = requestedShipDateTo,
        ).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getDetail(orderId: Long): OutboundOrderDetailResponse {
        val order = outboundOrderRepository.findById(orderId)
            .orElseThrow { NotFoundException("출고 지시를 찾을 수 없습니다: $orderId") }
        val lines = outboundOrderLineRepository.findByOutboundOrderIdOrderByLineNo(orderId)
        return OutboundOrderDetailResponse(
            order = order.toResponse(),
            lines = lines.map { it.toResponse() },
        )
    }
}


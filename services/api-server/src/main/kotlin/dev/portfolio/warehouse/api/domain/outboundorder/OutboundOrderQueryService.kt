package dev.portfolio.warehouse.api.domain.outboundorder

import dev.portfolio.warehouse.api.support.error.NotFoundException
import jakarta.persistence.criteria.JoinType
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
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
    ): List<OutboundOrderResponse> {
        val orders = outboundOrderRepository.findAll(
            outboundOrderSearchSpec(
                clientCompanyId = clientCompanyId,
                status = status,
                requestedShipDateFrom = requestedShipDateFrom,
                requestedShipDateTo = requestedShipDateTo,
            ),
            Sort.by(Sort.Direction.DESC, "id"),
        )
        return orders.map { order ->
            val lines = outboundOrderLineRepository.findByOutboundOrderIdOrderByLineNo(requireNotNull(order.id))
            order.toResponse(lines)
        }
    }

    @Transactional(readOnly = true)
    fun getDetail(orderId: Long): OutboundOrderDetailResponse {
        val order = outboundOrderRepository.findById(orderId)
            .orElseThrow { NotFoundException("출고 지시를 찾을 수 없습니다: $orderId") }
        val lines = outboundOrderLineRepository.findByOutboundOrderIdOrderByLineNo(orderId)
        return OutboundOrderDetailResponse(
            order = order.toResponse(lines),
            lines = lines.map { it.toResponse() },
        )
    }
}

private fun outboundOrderSearchSpec(
    clientCompanyId: Long?,
    status: OutboundOrderStatus?,
    requestedShipDateFrom: LocalDate?,
    requestedShipDateTo: LocalDate?,
): Specification<OutboundOrderEntity> =
    Specification { root, _, criteriaBuilder ->
        root.fetch<Any, Any>("clientCompany", JoinType.LEFT)
        root.fetch<Any, Any>("warehouse", JoinType.LEFT)

        val predicates = listOfNotNull(
            clientCompanyId?.let {
                criteriaBuilder.equal(root.get<Any>("clientCompany").get<Long>("id"), it)
            },
            status?.let {
                criteriaBuilder.equal(root.get<OutboundOrderStatus>("status"), it)
            },
            requestedShipDateFrom?.let {
                criteriaBuilder.greaterThanOrEqualTo(root.get("requestedShipDate"), it)
            },
            requestedShipDateTo?.let {
                criteriaBuilder.lessThanOrEqualTo(root.get("requestedShipDate"), it)
            },
        )

        criteriaBuilder.and(*predicates.toTypedArray())
    }

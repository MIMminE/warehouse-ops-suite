package dev.portfolio.warehouse.api.domain.outboundorder

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface OutboundOrderRepository : JpaRepository<OutboundOrderEntity, Long> {
    fun existsByClientCompanyIdAndOutboundOrderNo(clientCompanyId: Long, outboundOrderNo: String): Boolean

    @Query(
        """
        select o
        from OutboundOrderEntity o
        where (:clientCompanyId is null or o.clientCompany.id = :clientCompanyId)
          and (:status is null or o.status = :status)
          and (:requestedShipDateFrom is null or o.requestedShipDate >= :requestedShipDateFrom)
          and (:requestedShipDateTo is null or o.requestedShipDate <= :requestedShipDateTo)
        order by o.id desc
        """,
    )
    fun search(
        @Param("clientCompanyId") clientCompanyId: Long?,
        @Param("status") status: OutboundOrderStatus?,
        @Param("requestedShipDateFrom") requestedShipDateFrom: LocalDate?,
        @Param("requestedShipDateTo") requestedShipDateTo: LocalDate?,
    ): List<OutboundOrderEntity>
}

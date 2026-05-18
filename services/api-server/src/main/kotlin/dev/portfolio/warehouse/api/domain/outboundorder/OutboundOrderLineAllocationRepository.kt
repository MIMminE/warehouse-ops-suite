package dev.portfolio.warehouse.api.domain.outboundorder

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OutboundOrderLineAllocationRepository : JpaRepository<OutboundOrderLineAllocationEntity, Long> {
    fun findByOutboundOrderLineId(outboundOrderLineId: Long): List<OutboundOrderLineAllocationEntity>

    @Query(
        """
        select a
        from OutboundOrderLineAllocationEntity a
        join fetch a.outboundOrderLine line
        join fetch a.location
        join fetch a.sku
        where line.id in :lineIds
        order by line.id asc, a.id asc
        """,
    )
    fun findByOutboundOrderLineIds(
        @Param("lineIds") lineIds: Collection<Long>,
    ): List<OutboundOrderLineAllocationEntity>
}


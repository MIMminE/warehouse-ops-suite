package dev.portfolio.warehouse.api.domain.outboundorder

import dev.portfolio.warehouse.api.domain.inventory.InventoryEntity
import dev.portfolio.warehouse.api.domain.product.SkuEntity
import dev.portfolio.warehouse.api.domain.warehouse.LocationEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "outbound_order_line_allocations")
class OutboundOrderLineAllocationEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "outbound_order_line_id", nullable = false)
    var outboundOrderLine: OutboundOrderLineEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    var inventory: InventoryEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    var location: LocationEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    var sku: SkuEntity,

    @Column(nullable = false)
    var allocatedQuantity: Int,

    @Column(nullable = false)
    var pickedQuantity: Int = 0,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}


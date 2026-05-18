package dev.portfolio.warehouse.api.domain.picking

import dev.portfolio.warehouse.api.domain.outbound.OutboundWaveEntity
import dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderLineAllocationEntity
import dev.portfolio.warehouse.api.domain.outboundorder.OutboundOrderLineEntity
import dev.portfolio.warehouse.api.domain.product.SkuEntity
import dev.portfolio.warehouse.api.domain.warehouse.LocationEntity
import dev.portfolio.warehouse.api.domain.warehouse.WarehouseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "picking_tasks")
class PickingTaskEntity(
    @Column(nullable = false, unique = true, length = 50)
    var taskNo: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "outbound_wave_id", nullable = false)
    var outboundWave: OutboundWaveEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_order_line_id")
    var outboundOrderLine: OutboundOrderLineEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_order_line_allocation_id")
    var outboundOrderLineAllocation: OutboundOrderLineAllocationEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var warehouse: WarehouseEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_location_id", nullable = false)
    var sourceLocation: LocationEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    var sku: SkuEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: PickingTaskStatus = PickingTaskStatus.READY,

    @Column(nullable = false)
    var requestedQuantity: Int,

    @Column(nullable = false)
    var pickedQuantity: Int = 0,

    @Column(length = 100)
    var assignedWorker: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}

package dev.portfolio.warehouse.api.domain.putaway

import dev.portfolio.warehouse.api.domain.product.SkuEntity
import dev.portfolio.warehouse.api.domain.receiving.ReceivingOrderLineEntity
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
@Table(name = "putaway_tasks")
class PutawayTaskEntity(
    @Column(nullable = false, unique = true, length = 50)
    var taskNo: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiving_order_line_id", nullable = false)
    var receivingOrderLine: ReceivingOrderLineEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var warehouse: WarehouseEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_location_id", nullable = false)
    var targetLocation: LocationEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    var sku: SkuEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: PutawayTaskStatus = PutawayTaskStatus.READY,

    @Column(nullable = false)
    var putawayQuantity: Int,

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


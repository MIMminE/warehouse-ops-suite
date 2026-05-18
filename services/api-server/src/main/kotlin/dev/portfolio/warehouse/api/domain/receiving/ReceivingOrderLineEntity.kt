package dev.portfolio.warehouse.api.domain.receiving

import dev.portfolio.warehouse.api.domain.product.SkuEntity
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
@Table(name = "receiving_order_lines")
class ReceivingOrderLineEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiving_order_id", nullable = false)
    var receivingOrder: ReceivingOrderEntity,

    @Column(nullable = false)
    var lineNo: Int,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    var sku: SkuEntity,

    @Column(nullable = false)
    var requestedQuantity: Int,

    @Column(nullable = false)
    var receivedQuantity: Int = 0,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}


package dev.portfolio.warehouse.api.domain.outbound

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
@Table(name = "outbound_waves")
class OutboundWaveEntity(
    @Column(nullable = false, unique = true, length = 50)
    var waveNo: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var warehouse: WarehouseEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: OutboundWaveStatus = OutboundWaveStatus.READY,

    @Column(nullable = false, length = 100)
    var requestedBy: String,

    @Column(length = 500)
    var memo: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}


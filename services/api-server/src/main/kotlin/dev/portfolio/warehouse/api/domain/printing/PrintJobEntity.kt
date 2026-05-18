package dev.portfolio.warehouse.api.domain.printing

import dev.portfolio.warehouse.api.domain.outbound.OutboundWaveEntity
import dev.portfolio.warehouse.api.domain.picking.PickingTaskEntity
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
@Table(name = "print_jobs")
class PrintJobEntity(
    @Column(nullable = false, unique = true, length = 50)
    var jobNo: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_wave_id")
    var outboundWave: OutboundWaveEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picking_task_id")
    var pickingTask: PickingTaskEntity? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: PrintJobStatus = PrintJobStatus.REQUESTED,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var documentType: DocumentType,

    @Column(length = 150)
    var printerName: String? = null,

    @Column(length = 500)
    var failureReason: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}


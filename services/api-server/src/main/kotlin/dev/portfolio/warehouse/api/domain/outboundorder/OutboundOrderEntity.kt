package dev.portfolio.warehouse.api.domain.outboundorder

import dev.portfolio.warehouse.api.domain.client.ClientCompanyEntity
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
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "outbound_orders")
class OutboundOrderEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_company_id", nullable = false)
    var clientCompany: ClientCompanyEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var warehouse: WarehouseEntity,

    @Column(nullable = false, length = 50)
    var outboundOrderNo: String,

    @Column(length = 100)
    var externalReferenceNo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var intakeSource: OutboundOrderIntakeSource,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: OutboundOrderStatus = OutboundOrderStatus.RECEIVED,

    @Column(nullable = false, length = 100)
    var receiverName: String,

    @Column(nullable = false, length = 50)
    var receiverPhone: String,

    @Column(nullable = false, length = 20)
    var zipCode: String,

    @Column(nullable = false, length = 255)
    var address1: String,

    @Column(length = 255)
    var address2: String? = null,

    @Column(length = 500)
    var deliveryMemo: String? = null,

    var requestedShipDate: LocalDate? = null,

    var orderedAt: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}


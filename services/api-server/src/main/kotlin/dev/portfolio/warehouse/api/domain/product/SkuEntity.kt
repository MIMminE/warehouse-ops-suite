package dev.portfolio.warehouse.api.domain.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "skus")
class SkuEntity(
    @Column(nullable = false, unique = true, length = 80)
    var code: String,

    @Column(nullable = false, length = 150)
    var name: String,

    @Column(unique = true, length = 100)
    var barcode: String? = null,

    @Column(nullable = false, length = 20)
    var unit: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}


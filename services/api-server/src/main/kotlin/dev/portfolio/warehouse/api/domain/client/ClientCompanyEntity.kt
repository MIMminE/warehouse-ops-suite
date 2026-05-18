package dev.portfolio.warehouse.api.domain.client

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "client_companies")
class ClientCompanyEntity(
    @Column(nullable = false, unique = true, length = 50)
    var code: String,

    @Column(nullable = false, length = 150)
    var name: String,

    @Column(unique = true, length = 50)
    var businessRegistrationNo: String? = null,

    @Column(length = 100)
    var contactName: String? = null,

    @Column(length = 150)
    var contactEmail: String? = null,

    @Column(nullable = false)
    var active: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}


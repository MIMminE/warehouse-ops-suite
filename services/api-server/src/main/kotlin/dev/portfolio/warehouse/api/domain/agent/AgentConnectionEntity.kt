package dev.portfolio.warehouse.api.domain.agent

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "agent_connections")
class AgentConnectionEntity(
    @Column(nullable = false, unique = true, length = 80)
    var agentId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var agentType: AgentType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: AgentStatus = AgentStatus.OFFLINE,

    @Column(length = 255)
    var endpoint: String? = null,

    var lastSeenAt: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}


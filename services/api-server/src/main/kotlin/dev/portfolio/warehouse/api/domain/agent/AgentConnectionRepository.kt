package dev.portfolio.warehouse.api.domain.agent

import org.springframework.data.jpa.repository.JpaRepository

interface AgentConnectionRepository : JpaRepository<AgentConnectionEntity, Long> {
    fun existsByAgentId(agentId: String): Boolean
}


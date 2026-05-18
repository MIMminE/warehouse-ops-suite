package dev.portfolio.warehouse.api.domain.putaway

import org.springframework.data.jpa.repository.JpaRepository

interface PutawayTaskRepository : JpaRepository<PutawayTaskEntity, Long> {
    fun existsByTaskNo(taskNo: String): Boolean
}


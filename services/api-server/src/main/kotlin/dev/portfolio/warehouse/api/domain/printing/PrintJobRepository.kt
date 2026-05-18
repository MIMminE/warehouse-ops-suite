package dev.portfolio.warehouse.api.domain.printing

import org.springframework.data.jpa.repository.JpaRepository

interface PrintJobRepository : JpaRepository<PrintJobEntity, Long> {
    fun existsByJobNo(jobNo: String): Boolean
}


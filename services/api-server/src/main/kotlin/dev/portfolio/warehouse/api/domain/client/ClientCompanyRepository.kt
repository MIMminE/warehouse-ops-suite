package dev.portfolio.warehouse.api.domain.client

import org.springframework.data.jpa.repository.JpaRepository

interface ClientCompanyRepository : JpaRepository<ClientCompanyEntity, Long> {
    fun existsByCode(code: String): Boolean
}


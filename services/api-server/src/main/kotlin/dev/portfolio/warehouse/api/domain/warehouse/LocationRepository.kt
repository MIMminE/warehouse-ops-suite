package dev.portfolio.warehouse.api.domain.warehouse

import org.springframework.data.jpa.repository.JpaRepository

interface LocationRepository : JpaRepository<LocationEntity, Long>


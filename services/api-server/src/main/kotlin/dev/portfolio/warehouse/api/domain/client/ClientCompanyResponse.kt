package dev.portfolio.warehouse.api.domain.client

data class ClientCompanyResponse(
    val id: Long,
    val code: String,
    val name: String,
    val active: Boolean,
)

fun ClientCompanyEntity.toResponse(): ClientCompanyResponse =
    ClientCompanyResponse(
        id = requireNotNull(id),
        code = code,
        name = name,
        active = active,
    )

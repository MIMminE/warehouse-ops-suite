package dev.portfolio.warehouse.api.support.error

open class ApiException(
    val code: String,
    override val message: String,
) : RuntimeException(message)

class BadRequestException(message: String) : ApiException("BAD_REQUEST", message)

class DuplicateResourceException(message: String) : ApiException("DUPLICATE_RESOURCE", message)

class InsufficientStockException(message: String) : ApiException("INSUFFICIENT_STOCK", message)

class NotFoundException(message: String) : ApiException("NOT_FOUND", message)


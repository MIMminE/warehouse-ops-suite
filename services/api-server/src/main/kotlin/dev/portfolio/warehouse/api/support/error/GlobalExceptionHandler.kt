package dev.portfolio.warehouse.api.support.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(exception: NotFoundException): ResponseEntity<ApiErrorResponse> =
        error(HttpStatus.NOT_FOUND, exception)

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicate(exception: DuplicateResourceException): ResponseEntity<ApiErrorResponse> =
        error(HttpStatus.CONFLICT, exception)

    @ExceptionHandler(BadRequestException::class, InsufficientStockException::class)
    fun handleBadRequest(exception: ApiException): ResponseEntity<ApiErrorResponse> =
        error(HttpStatus.BAD_REQUEST, exception)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val message = exception.bindingResult.fieldErrors.firstOrNull()?.let {
            "${it.field}: ${it.defaultMessage}"
        } ?: "요청 값이 올바르지 않습니다."
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(code = "VALIDATION_ERROR", message = message))
    }

    private fun error(
        status: HttpStatus,
        exception: ApiException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(status)
            .body(ApiErrorResponse(code = exception.code, message = exception.message))
}


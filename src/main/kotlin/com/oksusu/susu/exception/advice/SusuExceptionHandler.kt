package com.oksusu.susu.exception.advice

import com.oksusu.susu.common.dto.ErrorResponse
import com.oksusu.susu.exception.SusuException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class SusuExceptionHandler {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(SusuException::class)
    protected fun handleBusinessException(e: SusuException): ResponseEntity<ErrorResponse> {
        logger.warn { "BusinessException ${e.message}" }
        val response = ErrorResponse(
            errorCode = e.errorCode.name,
            reason = e.message ?: e.errorCode.description,
            extra = e.extra
        )
        return ResponseEntity(response, e.errorCode.status)
    }
}

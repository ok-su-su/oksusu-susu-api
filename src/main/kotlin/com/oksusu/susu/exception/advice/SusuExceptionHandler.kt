package com.oksusu.susu.exception.advice

import com.oksusu.susu.common.dto.ErrorResponse
import com.oksusu.susu.exception.BusinessException
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class SusuExceptionHandler {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(BusinessException::class)
    protected fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        logger.warn { "BusinessException ${e.message}" }
        val response = ErrorResponse(
            errorCode = e.errorCode.name,
            reason = e.message ?: e.errorCode.description,
            extra = e.extra
        )
        return ResponseEntity(response, e.errorCode.status)
    }
}

package com.oksusu.susu.api.exception.advice

import com.oksusu.susu.api.common.dto.ErrorResponse
import com.oksusu.susu.common.exception.InvalidTokenException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(InvalidTokenException::class)
    protected fun handleInvalidTokenException(e: InvalidTokenException): ResponseEntity<ErrorResponse> {
        logger.warn { "InvalidTokenException ${e.message}" }
        val response = ErrorResponse(
            errorCode = e.errorCode.name,
            reason = e.message ?: e.errorCode.description,
        )
        return ResponseEntity(response, e.errorCode.status)
    }
}

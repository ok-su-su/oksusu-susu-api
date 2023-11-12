package com.goofy.boilerplate.exception.advice

import com.goofy.boilerplate.common.dto.ErrorResponse
import kotlinx.coroutines.CancellationException
import mu.KotlinLogging
import org.hibernate.TypeMismatchException
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import javax.validation.ConstraintViolationException

@RestControllerAdvice
class ExceptionHandler {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(WebExchangeBindException::class)
    protected fun handleWebExchangeBindException(
        e: WebExchangeBindException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "WebExchangeBindException ${e.message}, requestUri=${exchange.request.uri}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(e))
    }

    @ExceptionHandler(DecodingException::class)
    protected fun handleDecodingException(
        e: DecodingException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "DecodingException ${e.message}, requestUri=${exchange.request.uri}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(e))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    protected fun handleConstraintViolationException(
        e: ConstraintViolationException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "ConstraintViolationException ${e.message}, requestUri=${exchange.request.uri}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(e))
    }

    @ExceptionHandler(ServerWebInputException::class)
    protected fun handleServerWebInputException(
        e: ServerWebInputException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "ServerWebInputException ${e.message}, requestUri=${exchange.request.uri}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(e))
    }

    @ExceptionHandler(TypeMismatchException::class)
    protected fun handleTypeMismatchException(
        e: TypeMismatchException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "TypeMismatchException ${e.message}, requestUri=${exchange.request.uri}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(e))
    }

    /**
     * Coroutines Cancellation
     * - [Kotlin Docs](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.cancellation/)
     */
    @ExceptionHandler(CancellationException::class)
    protected fun handleCancellationException(
        e: CancellationException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "CancellationException ${e.message}, requestUri=${exchange.request.uri}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(e))
    }
}

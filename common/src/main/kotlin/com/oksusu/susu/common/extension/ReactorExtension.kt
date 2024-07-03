package com.oksusu.susu.common.extension

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger { }

suspend fun <T> Mono<T>.awaitSingleOrThrow(): T {
    return this.onErrorResume(WebClientResponseException::class.java) { ex ->
        logger.info { "${ex.request?.uri} ${ex.responseBodyAsString}" }
        Mono.error(ex)
    }.awaitSingle()
}

suspend fun <T> Mono<T>.awaitSingleOptionalOrThrow(): T? {
    return this.onErrorResume(WebClientResponseException::class.java) { ex ->
        logger.info { "${ex.request?.uri} ${ex.responseBodyAsString}" }
        Mono.error(ex)
    }.awaitSingleOrNull()
}

package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.event.model.SentryCaptureExceptionEvent
import com.oksusu.susu.api.extension.remoteIp
import com.oksusu.susu.common.extension.isProd
import com.oksusu.susu.common.extension.mdcCoroutineScope
import io.github.oshai.kotlinlogging.KotlinLogging
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component

@Component
class SentryCaptureExceptionEventListener(
    private val environment: Environment,
) {
    val logger = KotlinLogging.logger { }

    @EventListener
    fun execute(event: SentryCaptureExceptionEvent) {
        /** prod 환경에서만 작동 */
        if (!environment.isProd()) {
            return
        }

        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
            val throwable = event.exception
            val request = event.request

            throwable.stackTrace = throwable.suppressedExceptions.flatMap { suppressed ->
                suppressed.stackTrace.toList()
            }.toTypedArray()

            val url = request.uri.toString()
            val method = request.method.name()
            val body = DataBufferUtils.join(request.body)
                .map { dataBuffer ->
                    val bytes = ByteArray(dataBuffer.readableByteCount())
                    dataBuffer.read(bytes)
                    DataBufferUtils.release(dataBuffer)
                    bytes.decodeToString()
                }.awaitSingleOrNull() ?: "empty"
            val errorRequestParam = getRequestParam(request)
            val errorUserIP = request.remoteIp

            Sentry.configureScope { scope ->
                scope.setExtra("url", url)
                scope.setExtra("method", method)
                scope.setExtra("body", body)
                scope.setExtra("error request param", errorRequestParam)
                scope.setExtra("error user ip", errorUserIP)
            }

            Sentry.captureException(throwable)
        }
    }

    private fun getRequestParam(request: ServerHttpRequest): String {
        return request.queryParams.map { param ->
            val value = when (param.value.size == 1) {
                true -> param.value.firstOrNull()
                false -> param.value
            }
            "${param.key} : $value"
        }.joinToString("\n")
    }
}

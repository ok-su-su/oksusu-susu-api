package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.event.model.SlackErrorAlarmEvent
import com.oksusu.susu.api.extension.remoteIp
import com.oksusu.susu.client.slack.SlackClient
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.common.extension.format
import com.oksusu.susu.common.extension.isProd
import com.oksusu.susu.common.extension.mdcCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpRequest
import java.time.LocalDateTime

@SusuEventListener
class SlackErrorAlarmEventListener(
    private val environment: Environment,
    private val slackClient: SlackClient,
) {
    @EventListener
    fun execute(event: SlackErrorAlarmEvent) {
        /** prod 환경에서만 작동 */
        if (!environment.isProd()) {
            return
        }

        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
            val url = event.request.uri.toString()
            val method = event.request.method.toString()
            val errorMessage = event.exception.toString()
            val errorStack = getErrorStack(event.exception)
            val errorUserIP = event.request.remoteIp
            val errorRequestParam = getRequestParam(event.request)
            val body = DataBufferUtils.join(event.request.body)
                .map { dataBuffer ->
                    val bytes = ByteArray(dataBuffer.readableByteCount())
                    dataBuffer.read(bytes)
                    DataBufferUtils.release(dataBuffer)
                    bytes.decodeToString()
                }.awaitSingleOrNull() ?: ""

            ErrorMessage(
                url = url,
                method = method,
                errorMessage = errorMessage,
                errorStack = errorStack,
                errorUserIP = errorUserIP,
                errorRequestParam = errorRequestParam,
                body = body
            ).run { slackClient.sendError(this.message()) }
        }
    }

    private fun getErrorStack(e: Exception): String {
        val exceptionAsStrings = e.suppressedExceptions.flatMap { exception ->
            exception.stackTrace.map { stackTrace ->
                stackTrace.toString()
            }
        }.joinToString(" ")
        val cutLength = Math.min(exceptionAsStrings.length, 1000)
        return exceptionAsStrings.substring(0, cutLength)
    }

    private fun getRequestParam(request: ServerHttpRequest): String {
        return request.queryParams.map { param ->
            @Suppress("IMPLICIT_CAST_TO_ANY")
            val value = when (param.value.size == 1) {
                true -> param.value.firstOrNull()
                false -> param.value
            }
            "${param.key} : $value"
        }.joinToString("\n")
    }
}

private data class ErrorMessage(
    val url: String,
    val method: String,
    val errorMessage: String,
    val errorStack: String,
    val errorUserIP: String,
    val errorRequestParam: String,
    val body: String,
) {
    fun message(): SlackMessageModel {
        return SlackMessageModel(
            """
                * 에러 발생 ${LocalDateTime.now().format("yyyy-MM-dd HH:mm:ss")}*
                - User IP : $errorUserIP
                - Request Addr : $method - $url
                - Requset Param : $errorRequestParam
                - Request Body : $body
                - Message : $errorMessage
                - Stack Trace : $errorStack
                """.trimIndent()
        )
    }
}

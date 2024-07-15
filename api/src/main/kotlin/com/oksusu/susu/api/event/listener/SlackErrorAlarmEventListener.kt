package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.event.model.SlackErrorAlarmEvent
import com.oksusu.susu.api.extension.remoteIp
import com.oksusu.susu.api.extension.requestParam
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.client.slack.SlackClient
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.common.extension.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.core.io.buffer.DataBufferUtils
import java.time.LocalDateTime

@SusuEventListener
class SlackErrorAlarmEventListener(
    private val environment: Environment,
    private val slackClient: SlackClient,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    @EventListener
    fun execute(event: SlackErrorAlarmEvent) {
        /** prod 환경에서만 작동 */
        if (!environment.isProd()) {
            return
        }

        mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
            val url = event.request.uri.toString()
            val method = event.request.method.toString()
            val errorMessage = event.exception.toString()
            val errorStack = event.exception.supressedErrorStack
            val errorUserIP = event.request.remoteIp
            val errorRequestParam = event.request.requestParam
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

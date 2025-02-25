package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.auth.application.AuthFacade
import com.oksusu.susu.api.auth.resolver.TokenResolver
import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.event.model.DiscordErrorAlarmEvent
import com.oksusu.susu.api.event.model.ErrorMessage
import com.oksusu.susu.api.extension.remoteIp
import com.oksusu.susu.api.extension.requestParam
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.client.discord.DiscordClient
import com.oksusu.susu.common.extension.isProd
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.common.extension.supressedErrorStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.core.io.buffer.DataBufferUtils

@SusuEventListener
class DiscordErrorAlarmEventListener(
    private val environment: Environment,
    private val discordClient: DiscordClient,
    private val authFacade: AuthFacade,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    @EventListener
    fun execute(event: DiscordErrorAlarmEvent) {
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
            val token = TokenResolver(event.request).resolveToken().block()
            val uid = authFacade.getUidFromTokenOrNull(token!!) ?: -1L

            ErrorMessage(
                uid = uid,
                url = url,
                method = method,
                errorMessage = errorMessage,
                errorStack = errorStack,
                errorUserIP = errorUserIP,
                errorRequestParam = errorRequestParam,
                body = body
            ).run { discordClient.sendError(this.discordMessage()) }
        }
    }
}

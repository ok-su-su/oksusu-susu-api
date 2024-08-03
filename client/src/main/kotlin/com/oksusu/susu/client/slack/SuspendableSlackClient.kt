package com.oksusu.susu.client.slack

import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.model.FailedSentSlackMessageCache
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.client.config.SlackConfig
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.FailToExecuteException
import com.oksusu.susu.common.extension.awaitSingleOrThrow
import com.oksusu.susu.common.extension.withMDCContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.util.retry.Retry
import java.time.Duration
import java.time.LocalDateTime

class SuspendableSlackClient(
    private val webclient: WebClient,
    private val slackWebhookConfig: SlackConfig.SlackWebhookConfig,
    private val cacheService: CacheService,
) : SlackClient {
    private val logger = KotlinLogging.logger {  }

    override suspend fun sendSummary(message: SlackMessageModel): String {
        return sendMessage(message, slackWebhookConfig.summaryToken)
    }

    override suspend fun sendError(message: SlackMessageModel): String {
        return sendMessage(message, slackWebhookConfig.errorToken)
    }

    override suspend fun sendMessage(message: SlackMessageModel, token: String, withRecover: Boolean): String {
        return runCatching {
            withMDCContext(Dispatchers.IO) {
                webclient.post()
                    .uri("/${token}aasd")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(message)
                    .retrieve()
                    .bodyToMono<String>()
                    .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(500)))
                    .awaitSingleOrThrow()
            }
        }.onFailure {
            if (withRecover){
                recoverSendMessage(message, token)
            }
        }.getOrThrow()
    }

    private suspend fun recoverSendMessage(message: SlackMessageModel, token: String) {
        val model = FailedSentSlackMessageCache(
            token = token,
            message = message.text
        )

        cacheService.sSet(Cache.getFailedSentSlackMessageCache(LocalDateTime.now()), model)
    }
}

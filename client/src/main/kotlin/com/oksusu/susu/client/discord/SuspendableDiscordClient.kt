package com.oksusu.susu.client.discord

import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.model.FailedSentDiscordMessageCache
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.client.config.DiscordConfig
import com.oksusu.susu.client.discord.model.DiscordMessageModel
import com.oksusu.susu.common.extension.awaitSingleOrThrow
import com.oksusu.susu.common.extension.withMDCContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime

class SuspendableDiscordClient(
    private val webclient: WebClient,
    private val discordWebhookConfig: DiscordConfig.DiscordWebhookConfig,
    private val cacheService: CacheService,
) : DiscordClient {
    private val logger = KotlinLogging.logger { }

    override suspend fun sendSummary(message: DiscordMessageModel) {
        return sendMessage(message, discordWebhookConfig.summaryToken)
    }

    override suspend fun sendError(message: DiscordMessageModel) {
        return sendMessage(message, discordWebhookConfig.errorToken)
    }

    override suspend fun sendMessage(message: DiscordMessageModel, token: String, withRecover: Boolean) {
        runCatching {
            withMDCContext(Dispatchers.IO) {
                webclient.post()
                    .uri("/$token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(message)
                    .retrieve()
                    .toBodilessEntity()
//                    .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(500)))
                    .awaitSingleOrThrow()
            }
        }.onFailure {
            if (withRecover) {
//                recoverSendMessage(message, token)
            }
        }.getOrThrow()
    }

    private suspend fun recoverSendMessage(message: DiscordMessageModel, token: String) {
        val model = FailedSentDiscordMessageCache(
            token = token,
            message = message.content
        )

        cacheService.sSet(Cache.getFailedSentDiscordMessageCache(LocalDateTime.now()), model)
    }
}

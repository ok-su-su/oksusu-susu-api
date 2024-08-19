package com.oksusu.susu.batch.discord.job

import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.model.FailedSentDiscordMessageCache
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.client.discord.DiscordClient
import com.oksusu.susu.client.discord.model.DiscordMessageModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ResendFailedSentDiscordMessageJob(
    private val cacheService: CacheService,
    private val discordClient: DiscordClient,
) {
    private val logger = KotlinLogging.logger { }

    companion object {
        private const val RESEND_BEFORE_MINUTES = 1L
    }

    suspend fun resendFailedSentDiscordMessage() {
        logger.info { "start resend failed sent discord message" }

        // 1분 전에 실패한 것이 타겟 (현재가 24분이면 23분을 말하는 것)
        val targetTime = LocalDateTime.now().minusMinutes(RESEND_BEFORE_MINUTES)

        // 실패 메세지 조회 및 삭제
        val failedMessages = withContext(Dispatchers.IO) {
            cacheService.sGetMembers(Cache.getFailedSentDiscordMessageCache(targetTime))
        }

        withContext(Dispatchers.IO) {
            cacheService.sDelete(Cache.getFailedSentDiscordMessageCache(targetTime))
        }

        // 다수 메세지 token 별로 하나의 메세지로 병합
        val message = mergeFailedMessage(failedMessages)

        // 재전송
        runCatching {
            coroutineScope {
                val sendDeferreds = message.map { (token, message) ->
                    val discordMessageModel = DiscordMessageModel(content = message)

                    async(Dispatchers.IO) {
                        discordClient.sendMessage(
                            message = discordMessageModel,
                            token = token,
                            withRecover = false
                        )
                    }
                }.toTypedArray()

                awaitAll(*sendDeferreds)
            }
        }.onFailure {
            // 재전송 실패시 1분 뒤에 다시 보낼 수 있게, 1분 뒤에 보내는 메세지 목록에 추가
            logger.warn { "postpone resend discord message" }

            postponeResendTimeOfFailedMessage(targetTime, message)
        }

        logger.info { "finish resend failed sent discord message" }
    }

    private suspend fun mergeFailedMessage(failedMessages: List<FailedSentDiscordMessageCache>): Map<String, String> {
        val message = mutableMapOf<String, String>()

        failedMessages.forEach { model ->
            val recoverMsg = if (model.isStacked) {
                model.message
            } else {
                "[RECOVER - ${model.failedAt} discord failure] ${model.message}"
            }

            val stackedMessage = message[model.token]

            message[model.token] = if (stackedMessage == null) {
                recoverMsg
            } else {
                "$stackedMessage\n$recoverMsg"
            }
        }

        return message
    }

    private suspend fun postponeResendTimeOfFailedMessage(targetTime: LocalDateTime, message: Map<String, String>) {
        val nextTime = targetTime.plusMinutes(RESEND_BEFORE_MINUTES)

        coroutineScope {
            message.map { (token, message) ->
                val model = FailedSentDiscordMessageCache(
                    token = token,
                    message = message,
                    isStacked = true
                )

                async(Dispatchers.IO) {
                    cacheService.sSet(
                        cache = Cache.getFailedSentDiscordMessageCache(nextTime),
                        value = model
                    )
                }
            }
        }
    }
}

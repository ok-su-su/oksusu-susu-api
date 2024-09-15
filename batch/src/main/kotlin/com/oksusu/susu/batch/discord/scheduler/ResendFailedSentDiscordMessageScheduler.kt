package com.oksusu.susu.batch.discord.scheduler

import com.oksusu.susu.batch.discord.job.ResendFailedSentDiscordMessageJob
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.isProd
import com.oksusu.susu.common.extension.resolveCancellation
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ResendFailedSentDiscordMessageScheduler(
    private val environment: Environment,
    private val resendFailedSentDiscordMessageJob: ResendFailedSentDiscordMessageJob,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    private val logger = KotlinLogging.logger { }

//    @Scheduled(
//        fixedRate = 1000 * 60,
//        initialDelayString = "\${oksusu.scheduled-tasks.resend-failed-sent-discord-message.initial-delay:100}"
//    )
    fun resendFailedSentDiscordMessageJob() {
        if (environment.isProd()) {
            CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
                runCatching {
                    resendFailedSentDiscordMessageJob.resendFailedSentDiscordMessage()
                }.onFailure { e ->
                    logger.resolveCancellation("[BATCH] fail to run resendFailedSentDiscordMessageJob", e)
                }
            }
        }
    }
}

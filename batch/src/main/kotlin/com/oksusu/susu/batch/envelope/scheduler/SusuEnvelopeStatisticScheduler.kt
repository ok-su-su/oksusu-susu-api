package com.oksusu.susu.batch.envelope.scheduler

import com.oksusu.susu.batch.envelope.job.RefreshSusuEnvelopeStatisticJob
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.resolveCancellation
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SusuEnvelopeStatisticScheduler(
    private val refreshSusuEnvelopeStatisticJob: RefreshSusuEnvelopeStatisticJob,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    private val logger = KotlinLogging.logger { }

    @Scheduled(
        fixedRate = 1000 * 60 * 60,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-susu-envelope-statistic.initial-delay:100}"
    )
    fun refreshSusuEnvelopeStatistic() {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            runCatching {
                refreshSusuEnvelopeStatisticJob.refreshSusuEnvelopeStatistic()
            }.onFailure { e ->
                logger.resolveCancellation("[BATCH] fail to run refreshSusuEnvelopeStatistic", e)
            }
        }
    }
}

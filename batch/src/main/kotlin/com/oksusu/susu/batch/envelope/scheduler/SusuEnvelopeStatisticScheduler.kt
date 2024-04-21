package com.oksusu.susu.batch.envelope.scheduler

import com.oksusu.susu.batch.envelope.job.RefreshSusuEnvelopeStatisticJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SusuEnvelopeStatisticScheduler(
    private val refreshSusuEnvelopeStatisticJob: RefreshSusuEnvelopeStatisticJob,
) {
    @Scheduled(
        fixedRate = 1000 * 60 * 60,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-susu-envelope-statistic.initial-delay:100}"
    )
    fun refreshSusuEnvelopeStatistic() {
        CoroutineScope(Dispatchers.IO).launch {
            refreshSusuEnvelopeStatisticJob.refreshSusuEnvelopeStatistic()
        }
    }
}

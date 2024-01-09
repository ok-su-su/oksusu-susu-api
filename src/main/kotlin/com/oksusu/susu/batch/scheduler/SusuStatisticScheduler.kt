package com.oksusu.susu.batch.scheduler

import com.oksusu.susu.batch.job.RefreshSusuStatisticJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SusuStatisticScheduler(
    private val refreshSusuStatisticJob: RefreshSusuStatisticJob,
) {
    @Scheduled(
        fixedRate = 1000 * 60 * 60,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-susu-statistic.initial-delay:100}"
    )
    fun refreshSusuStatistic() {
        CoroutineScope(Dispatchers.IO).launch {
            refreshSusuStatisticJob.refreshSusuStatistic()
        }
    }
}

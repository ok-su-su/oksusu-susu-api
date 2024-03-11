package com.oksusu.susu.api.batch.scheduler

import com.oksusu.susu.api.batch.job.SusuStatisticsDailySummaryJob
import com.oksusu.susu.api.batch.job.SusuStatisticsHourSummaryJob
import com.oksusu.susu.common.config.environment.EnvironmentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Profile(com.oksusu.susu.common.config.environment.EnvironmentType.PROFILE_PROD)
@Component
class SusuStatisticsSummaryScheduler(
    private val hourSummaryJob: SusuStatisticsHourSummaryJob,
    private val dailySummaryJob: SusuStatisticsDailySummaryJob,
) {
    @Scheduled(cron = "0 0 0/1 * * *")
    fun runHourSummary() {
        CoroutineScope(Dispatchers.IO).launch {
            hourSummaryJob.runHourSummaryJob()
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    fun runDailySummary() {
        CoroutineScope(Dispatchers.IO).launch {
            dailySummaryJob.runDailySummaryJob()
        }
    }
}

package com.oksusu.susu.batch.summary.scheduler

import com.oksusu.susu.batch.summary.job.SusuStatisticsDailySummaryJob
import com.oksusu.susu.batch.summary.job.SusuStatisticsHourSummaryJob
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.config.environment.EnvironmentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Profile(EnvironmentType.PROFILE_PROD)
@Component
class SusuStatisticsSummaryScheduler(
    private val hourSummaryJob: SusuStatisticsHourSummaryJob,
    private val dailySummaryJob: SusuStatisticsDailySummaryJob,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    @Scheduled(cron = "0 0 0/1 * * *")
    fun runHourSummary() {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            hourSummaryJob.runHourSummaryJob()
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    fun runDailySummary() {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            dailySummaryJob.runDailySummaryJob()
        }
    }
}

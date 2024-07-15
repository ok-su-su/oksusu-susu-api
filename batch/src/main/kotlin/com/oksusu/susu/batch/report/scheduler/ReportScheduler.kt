package com.oksusu.susu.batch.report.scheduler

import com.oksusu.susu.batch.report.job.ImposeSanctionsAboutReportJob
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReportScheduler(
    private val imposeSanctionsAboutReportJob: ImposeSanctionsAboutReportJob,
    private val errorPublishingCoroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    @Scheduled(cron = "0 0 0 * * *")
    fun imposeSanctionsAboutReportForDay() {
        CoroutineScope(Dispatchers.IO + errorPublishingCoroutineExceptionHandler.handler).launch {
            imposeSanctionsAboutReportJob.imposeSanctionsAboutReportForDay()
        }
    }
}

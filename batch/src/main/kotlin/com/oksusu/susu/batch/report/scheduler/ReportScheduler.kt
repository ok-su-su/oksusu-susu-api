package com.oksusu.susu.batch.report.scheduler

import com.oksusu.susu.batch.report.job.ImposeSanctionsAboutReportJob
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.resolveCancellation
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReportScheduler(
    private val imposeSanctionsAboutReportJob: ImposeSanctionsAboutReportJob,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    private val logger = KotlinLogging.logger { }

    @Scheduled(cron = "0 0 0 * * *")
    fun imposeSanctionsAboutReportForDay() {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            runCatching {
                imposeSanctionsAboutReportJob.imposeSanctionsAboutReportForDay()
            }.onFailure { e ->
                logger.resolveCancellation("[BATCH] fail to run imposeSanctionsAboutReportForDay", e)
            }
        }
    }
}

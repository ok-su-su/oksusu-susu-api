package com.oksusu.susu.batch.report.scheduler

import com.oksusu.susu.batch.report.job.ImposeSanctionsAboutReportJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReportScheduler(
    private val imposeSanctionsAboutReportJob: ImposeSanctionsAboutReportJob,
) {
    @Scheduled(cron = "0 0 0 * * *")
    fun imposeSanctionsAboutReport(){
        CoroutineScope(Dispatchers.IO).launch {
            imposeSanctionsAboutReportJob.imposeSanctionsAboutReport()
        }
    }
}

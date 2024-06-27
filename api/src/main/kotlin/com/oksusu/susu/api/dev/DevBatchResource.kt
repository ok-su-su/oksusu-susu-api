package com.oksusu.susu.api.dev

import com.oksusu.susu.api.auth.model.AdminUser
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.batch.envelope.job.RefreshSusuEnvelopeStatisticJob
import com.oksusu.susu.batch.report.job.ImposeSanctionsAboutReportJob
import com.oksusu.susu.batch.summary.job.SusuStatisticsDailySummaryJob
import com.oksusu.susu.batch.summary.job.SusuStatisticsHourSummaryJob
import com.oksusu.susu.batch.user.job.DeleteWithdrawUserDataJob
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.DEV_BATCH_SWAGGER_TAG, description = "개발용 Batch 관리 API")
@RestController
@RequestMapping(value = ["/api/v1/dev/batches"], produces = [MediaType.APPLICATION_JSON_VALUE])
class DevBatchResource(
    private val susuStatisticsHourSummaryJob: SusuStatisticsHourSummaryJob,
    private val susuStatisticsDailySummaryJob: SusuStatisticsDailySummaryJob,
    private val susuEnvelopeStatisticJob: RefreshSusuEnvelopeStatisticJob,
    private val deleteWithdrawUserDataJob: DeleteWithdrawUserDataJob,
    private val imposeSanctionsAboutReportJob: ImposeSanctionsAboutReportJob,
) {
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "hour summary 호출")
    @GetMapping("/hour-summaries")
    suspend fun getHourSummaries(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            susuStatisticsHourSummaryJob.runHourSummaryJob()
        }
    }

    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "daily summary 호출")
    @GetMapping("/daily-summaries")
    suspend fun getDailySummaries(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            susuStatisticsDailySummaryJob.runDailySummaryJob()
        }
    }

    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "refresh susu envelope statistic 호출")
    @GetMapping("/refresh-susu-envelope-statistic")
    suspend fun refreshSusuEnvelopeStatistic(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            susuEnvelopeStatisticJob.refreshSusuEnvelopeStatistic()
        }
    }

    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "delete withdraw user data 호출")
    @GetMapping("/delete-withdraw-user-data")
    suspend fun deleteWithdrawUserData(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            deleteWithdrawUserDataJob.deleteWithdrawUserData()
        }
    }

    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "impose sanctions about report 호출")
    @GetMapping("/impose-sanction-about-report")
    suspend fun imposeSanctionsAboutReport(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            imposeSanctionsAboutReportJob.imposeSanctionsAboutReport()
        }
    }
}

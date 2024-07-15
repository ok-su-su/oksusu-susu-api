package com.oksusu.susu.api.dev

import com.oksusu.susu.api.auth.model.AdminUser
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.batch.envelope.job.RefreshSusuEnvelopeStatisticJob
import com.oksusu.susu.batch.report.job.ImposeSanctionsAboutReportJob
import com.oksusu.susu.batch.summary.job.SusuStatisticsDailySummaryJob
import com.oksusu.susu.batch.summary.job.SusuStatisticsHourSummaryJob
import com.oksusu.susu.batch.user.job.DeleteWithdrawUserDataJob
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.LoggingCoroutineExceptionHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.coroutines.AbstractCoroutineContextElement

@Tag(name = SwaggerTag.DEV_BATCH_SWAGGER_TAG, description = "개발용 Batch 관리 API")
@RestController
@RequestMapping(value = ["/api/v1/dev/batches"], produces = [MediaType.APPLICATION_JSON_VALUE])
class DevBatchResource(
    private val susuStatisticsHourSummaryJob: SusuStatisticsHourSummaryJob,
    private val susuStatisticsDailySummaryJob: SusuStatisticsDailySummaryJob,
    private val susuEnvelopeStatisticJob: RefreshSusuEnvelopeStatisticJob,
    private val deleteWithdrawUserDataJob: DeleteWithdrawUserDataJob,
    private val imposeSanctionsAboutReportJob: ImposeSanctionsAboutReportJob,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "hour summary 호출")
    @GetMapping("/hour-summaries")
    suspend fun getHourSummaries(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            susuStatisticsHourSummaryJob.runHourSummaryJob()
        }
    }

    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "daily summary 호출")
    @GetMapping("/daily-summaries")
    suspend fun getDailySummaries(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            susuStatisticsDailySummaryJob.runDailySummaryJob()
        }
    }

    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "refresh susu envelope statistic 호출")
    @GetMapping("/refresh-susu-envelope-statistic")
    suspend fun refreshSusuEnvelopeStatistic(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            susuEnvelopeStatisticJob.refreshSusuEnvelopeStatistic()
        }
    }

    /** 서비스 시작부터 지금까지 모든 탈퇴 유저의 데이터를 삭제한다. */
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "delete withdraw user data 호출")
    @GetMapping("/delete-withdraw-user-data")
    suspend fun deleteWithdrawUserData(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            deleteWithdrawUserDataJob.deleteWithdrawUserData()
        }
    }

    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "delete withdraw user data for week 호출")
    @GetMapping("/delete-withdraw-user-data-for-week")
    suspend fun deleteWithdrawUserDataForWeek(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            deleteWithdrawUserDataJob.deleteWithdrawUserDataForWeek()
        }
    }

    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "impose sanctions about report for day 호출")
    @GetMapping("/impose-sanction-about-report-for-day")
    suspend fun imposeSanctionsAboutReportForDay(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            imposeSanctionsAboutReportJob.imposeSanctionsAboutReportForDay()
        }
    }

    /** 서비스 시작부터 현 시점까지 기록된 report의 수를 캐싱한다. */
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "update report count 호출")
    @GetMapping("/update-report-count")
    suspend fun updateReportCount(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            imposeSanctionsAboutReportJob.updateReportCount()
        }
    }

    /** 서비스 시작 시점부터 지금까지의 유저 커뮤니티 처벌 카운트를 처리합니다. */
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "update user community punish count 호출")
    @GetMapping("/update-user-community-punish-count")
    suspend fun updateUserCommunityPunishCount(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            imposeSanctionsAboutReportJob.updateUserCommunityPunishCount()
        }
    }

    /** 수수 통계 캐싱 데이터를 초기화합니다. */
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "refresh susu envelope statistic amount 호출")
    @GetMapping("/refresh-susu-envelope-statistic-amount")
    suspend fun refreshSusuEnvelopeStatisticAmount(
        adminUser: AdminUser,
    ) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            susuEnvelopeStatisticJob.refreshSusuEnvelopeStatisticAmount()
        }
    }
}

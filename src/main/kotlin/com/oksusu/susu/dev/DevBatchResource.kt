package com.oksusu.susu.dev

import com.oksusu.susu.batch.job.SusuStatisticsDailySummaryJob
import com.oksusu.susu.batch.job.SusuStatisticsHourSummaryJob
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.extension.withMDCContext
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
) {
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "hour summary 호출")
    @GetMapping("/hour-summaries")
    suspend fun getHourSummaries() {
        CoroutineScope(Dispatchers.IO.withMDCContext()).launch {
            susuStatisticsHourSummaryJob.runHourSummaryJob()
        }
    }

    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "daily summary 호출")
    @GetMapping("/daily-summaries")
    suspend fun getDailySummaries() {
        CoroutineScope(Dispatchers.IO.withMDCContext()).launch {
            susuStatisticsDailySummaryJob.runDailySummaryJob()
        }
    }
}

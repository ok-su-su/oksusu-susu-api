package com.oksusu.susu.batch.job

import com.oksusu.susu.client.slack.SlackClient
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.extension.format
import com.oksusu.susu.log.application.SystemActionLogService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SusuStatisticsHourSummaryJob(
    private val slackClient: SlackClient,
    private val systemActionLogService: SystemActionLogService,
    private val envelopeService: EnvelopeService,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun runHourSummaryJob() {
        val now = LocalDateTime.now()
        val beforeOneHour = now.minusHours(1)

        val systemActionLogCount = systemActionLogService.countByCreatedAtBetween(beforeOneHour, now)

        val summaryMessage = HourSummaryMessage(now, systemActionLogCount)
        slackClient.send(summaryMessage.message())
    }
}

data class HourSummaryMessage(
    val now: LocalDateTime,
    val systemActionLogCount: Long,
) {
    fun message(): SlackMessageModel {
        return SlackMessageModel(
            """
                *시간단위 통계 알림${now.format("yyyyMMdd HH:mm:ss")}*
                - api 호출수 : $systemActionLogCount
            """.trimIndent()
        )
    }
}

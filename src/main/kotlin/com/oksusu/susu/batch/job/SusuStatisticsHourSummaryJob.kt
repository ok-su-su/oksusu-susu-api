package com.oksusu.susu.batch.job

import com.oksusu.susu.client.slack.SlackClient
import com.oksusu.susu.client.slack.model.SlackMessageModel
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SusuStatisticsHourSummaryJob(
    private val slackClient: SlackClient,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun runHourSummaryJob() {
        val summaryMessage = HourSummaryMessage()
        slackClient.send(summaryMessage.message())
    }
}

class HourSummaryMessage {
    fun message(): SlackMessageModel {
        return SlackMessageModel(
            """
                **시간단위 통계 알림${LocalDateTime.now()}**
            """.trimIndent()
        )
    }
}

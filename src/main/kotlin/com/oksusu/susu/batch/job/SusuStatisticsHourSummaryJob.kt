package com.oksusu.susu.batch.job

import com.oksusu.susu.client.slack.SlackClient
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.extension.format
import com.oksusu.susu.friend.application.FriendService
import com.oksusu.susu.log.application.SystemActionLogService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SusuStatisticsHourSummaryJob(
    private val slackClient: SlackClient,
    private val systemActionLogService: SystemActionLogService,
    private val envelopeService: EnvelopeService,
    private val friendService: FriendService,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun runHourSummaryJob() {
        val now = LocalDateTime.now()
        val beforeOneHour = now.minusHours(1)

        val systemActionLogCount = systemActionLogService.countByCreatedAtBetween(beforeOneHour, now)
        val envelopeCount = envelopeService.countByCreatedAtBetween(beforeOneHour, now)
        val friendCount = friendService.countByCreatedAtBetween(beforeOneHour, now)

        HourSummaryMessage(
            beforeOneHour = beforeOneHour,
            now = now,
            systemActionLogCount = systemActionLogCount,
            envelopeCount = envelopeCount,
            friendCount = friendCount
        ).run { slackClient.send(this.message()) }
    }
}

data class HourSummaryMessage(
    val beforeOneHour: LocalDateTime,
    val now: LocalDateTime,
    val systemActionLogCount: Long,
    val envelopeCount: Long,
    val friendCount: Long,
) {
    fun message(): SlackMessageModel {
        return SlackMessageModel(
            """
                *시간단위 통계 알림 ${now.format("yyyy-MM-dd HH:mm:ss")}*
                - ${beforeOneHour.format("yyyy-MM-dd HH:mm:ss")} ~ ${now.format("yyyy-MM-dd HH:mm:ss")}
                - api 호출수 : $systemActionLogCount
                - 봉투 생성수 : $envelopeCount
                - 친구 생성수 : $friendCount
            """.trimIndent()
        )
    }
}

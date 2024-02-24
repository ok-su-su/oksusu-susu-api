package com.oksusu.susu.batch.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.client.slack.SlackClient
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.application.LedgerService
import com.oksusu.susu.extension.format
import com.oksusu.susu.friend.application.FriendService
import com.oksusu.susu.log.application.SystemActionLogService
import com.oksusu.susu.user.application.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SusuStatisticsHourSummaryJob(
    private val slackClient: SlackClient,
    private val systemActionLogService: SystemActionLogService,
    private val ledgerService: LedgerService,
    private val envelopeService: EnvelopeService,
    private val friendService: FriendService,
    private val userService: UserService,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun runHourSummaryJob() {
        val now = LocalDateTime.now()
        val beforeOneHour = now.minusHours(1)

        parZip(
            { systemActionLogService.countByCreatedAtBetween(beforeOneHour, now) },
            { ledgerService.countByCreatedAtBetween(beforeOneHour, now) },
            { envelopeService.countByCreatedAtBetween(beforeOneHour, now) },
            { friendService.countByCreatedAtBetween(beforeOneHour, now) },
            { userService.countByCreatedAtBetween(beforeOneHour, now) }
        ) { systemActionLogCount, ledgerCount, envelopeCount, friendCount, userCount ->
            HourSummaryMessage(
                beforeOneHour = beforeOneHour,
                now = now,
                systemActionLogCount = systemActionLogCount,
                ledgerCount = ledgerCount,
                envelopeCount = envelopeCount,
                friendCount = friendCount,
                userCount
            )
        }.run { slackClient.send(this.message()) }
    }
}

data class HourSummaryMessage(
    val beforeOneHour: LocalDateTime,
    val now: LocalDateTime,
    val systemActionLogCount: Long,
    val ledgerCount: Long,
    val envelopeCount: Long,
    val friendCount: Long,
    val userCount: Long,
) {
    fun message(): SlackMessageModel {
        return SlackMessageModel(
            """
                *시간단위 통계 알림 ${now.format("yyyy-MM-dd HH:mm:ss")}*
                - ${beforeOneHour.format("HH:mm:ss")} ~ ${now.format("HH:mm:ss")}
                - api 호출수 : $systemActionLogCount
                - 장부 생성수 : $ledgerCount
                - 봉투 생성수 : $envelopeCount
                - 친구 생성수 : $friendCount
                - 유저 생성수 : $userCount
            """.trimIndent()
        )
    }
}

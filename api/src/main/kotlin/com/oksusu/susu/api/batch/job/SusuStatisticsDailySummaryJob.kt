package com.oksusu.susu.api.batch.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.api.envelope.application.EnvelopeService
import com.oksusu.susu.api.envelope.application.LedgerService
import com.oksusu.susu.api.friend.application.FriendService
import com.oksusu.susu.common.extension.format
import com.oksusu.susu.api.log.application.SystemActionLogService
import com.oksusu.susu.api.user.application.UserService
import com.oksusu.susu.api.user.application.UserWithdrawService
import com.oksusu.susu.client.slack.SlackClient
import com.oksusu.susu.client.slack.model.SlackMessageModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SusuStatisticsDailySummaryJob(
    private val slackClient: SlackClient,
    private val systemActionLogService: SystemActionLogService,
    private val userService: UserService,
    private val envelopeService: EnvelopeService,
    private val friendService: FriendService,
    private val ledgerService: LedgerService,
    private val userWithdrawService: UserWithdrawService,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun runDailySummaryJob() {
        val now = LocalDateTime.now()
        val beforeOneDay = now.minusDays(1)

        coroutineScope {
            parZip(
                Dispatchers.IO + MDCContext(),
                { systemActionLogService.countByCreatedAtBetween(beforeOneDay, now) },
                { userService.countByCreatedAtBetween(beforeOneDay, now) },
                { envelopeService.count() },
                { envelopeService.countByCreatedAtBetween(beforeOneDay, now) },
                { ledgerService.countByCreatedAtBetween(beforeOneDay, now) },
                { friendService.countByCreatedAtBetween(beforeOneDay, now) },
                { userWithdrawService.countByCreatedAtBetween(beforeOneDay, now) }
            ) {
                    systemActionLogCount,
                    userCount,
                    totalEnvelopeCount,
                    dailyEnvelopeCount,
                    dailyLedgerCount,
                    friendCount,
                    userWithdrawCount,
                ->
                DailySummaryMessage(
                    now = now,
                    systemActionLogCount = systemActionLogCount,
                    userCount = userCount,
                    totalEnvelopeCount = totalEnvelopeCount,
                    dailyEnvelopeCount = dailyEnvelopeCount,
                    dailyLedgerCount = dailyLedgerCount,
                    friendCount = friendCount,
                    userWithdrawCount = userWithdrawCount
                )
            }.run { slackClient.sendSummary(this.message()) }
        }
    }

    private data class DailySummaryMessage(
        val now: LocalDateTime,
        val systemActionLogCount: Long,
        val userCount: Long,
        val totalEnvelopeCount: Long,
        val dailyEnvelopeCount: Long,
        val dailyLedgerCount: Long,
        val friendCount: Long,
        val userWithdrawCount: Long,
    ) {
        fun message(): SlackMessageModel {
            return SlackMessageModel(
                """
                *일단위 통계 알림${now.format("yyyyMMdd HH:mm:ss")}*
                - 전날 종합 api 호출수 : $systemActionLogCount
                - 전날 종합  유저 가입수 : $userCount
                - 전날 종합 봉투 생성수 : $dailyEnvelopeCount
                - 전체 봉투 생성수 : $totalEnvelopeCount
                - 전날 종합 장부 생성수 : $dailyLedgerCount
                - 전날 종합 친구 생성수 : $friendCount
                - 전날 종합 유저 탈퇴수 : $userWithdrawCount
                """.trimIndent()
            )
        }
    }
}

package com.oksusu.susu.batch.job

import com.oksusu.susu.client.slack.SlackClient
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.extension.format
import com.oksusu.susu.envelope.application.LedgerService
import com.oksusu.susu.log.application.SystemActionLogService
import com.oksusu.susu.user.application.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SusuStatisticsDailySummaryJob(
    private val slackClient: SlackClient,
    private val systemActionLogService: SystemActionLogService,
    private val userService: UserService,
    private val envelopeService: EnvelopeService,
    private val ledgerService: LedgerService,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun runDailySummaryJob() {
        val now = LocalDateTime.now()
        val beforeOneDay = now.minusDays(1)

        val summaryMessage = coroutineScope {
            val systemActionLogCount = async(Dispatchers.IO) {
                systemActionLogService.countByCreatedAtBetween(beforeOneDay, now)
            }
            val userCount = async(Dispatchers.IO) {
                userService.countByCreatedAtBetween(beforeOneDay, now)
            }
            val totalEnvelopeCount = async(Dispatchers.IO) { envelopeService.count() }
            val dailyEnvelopeCount = async(Dispatchers.IO) {
                envelopeService.countByCreatedAtBetween(beforeOneDay, now)
            }
            val dailyLedgerCount = async(Dispatchers.IO) { ledgerService.countByCreatedAtBetween(beforeOneDay, now) }

            DailySummaryMessage(
                now = now,
                systemActionLogCount = systemActionLogCount.await(),
                userCount = userCount.await(),
                totalEnvelopeCount = totalEnvelopeCount.await(),
                dailyEnvelopeCount = dailyEnvelopeCount.await(),
                dailyLedgerCount = dailyLedgerCount.await()
            )
        }

        slackClient.send(summaryMessage.message())
    }
}

data class DailySummaryMessage(
    val now: LocalDateTime,
    val systemActionLogCount: Long,
    val userCount: Long,
    val totalEnvelopeCount: Long,
    val dailyEnvelopeCount: Long,
    val dailyLedgerCount: Long,
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
            """.trimIndent()
        )
    }
}

package com.oksusu.susu.batch.summary.job

import com.oksusu.susu.client.discord.DiscordClient
import com.oksusu.susu.client.discord.model.DiscordMessageModel
import com.oksusu.susu.common.extension.format
import com.oksusu.susu.common.extension.parZipWithMDC
import com.oksusu.susu.domain.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.domain.envelope.infrastructure.LedgerRepository
import com.oksusu.susu.domain.friend.infrastructure.FriendRepository
import com.oksusu.susu.domain.log.infrastructure.SystemActionLogRepository
import com.oksusu.susu.domain.report.infrastructure.ReportHistoryRepository
import com.oksusu.susu.domain.user.infrastructure.UserRepository
import com.oksusu.susu.domain.user.infrastructure.UserWithdrawRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SusuStatisticsDailySummaryJob(
    private val discordClient: DiscordClient,
    private val systemActionLogRepository: SystemActionLogRepository,
    private val userRepository: UserRepository,
    private val envelopeRepository: EnvelopeRepository,
    private val ledgerRepository: LedgerRepository,
    private val friendRepository: FriendRepository,
    private val userWithdrawRepository: UserWithdrawRepository,
    private val reportHistoryRepository: ReportHistoryRepository,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun runDailySummaryJob() {
        val now = LocalDateTime.now()
        val beforeOneDay = now.minusDays(1)

        parZipWithMDC(
            { withContext(Dispatchers.IO) { systemActionLogRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { userRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { envelopeRepository.count() } },
            { withContext(Dispatchers.IO) { envelopeRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { ledgerRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { friendRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { userWithdrawRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { reportHistoryRepository.countByCreatedAtBetween(beforeOneDay, now) } }
        ) {
                systemActionLogCount,
                userCount,
                totalEnvelopeCount,
                dailyEnvelopeCount,
                dailyLedgerCount,
                friendCount,
                userWithdrawCount,
                dailyReportHistoryCount,
            ->
            DailySummaryMessage(
                now = now,
                systemActionLogCount = systemActionLogCount,
                userCount = userCount,
                totalEnvelopeCount = totalEnvelopeCount,
                dailyEnvelopeCount = dailyEnvelopeCount,
                dailyLedgerCount = dailyLedgerCount,
                friendCount = friendCount,
                userWithdrawCount = userWithdrawCount,
                dailyReportHistoryCount = dailyReportHistoryCount
            )
        }.run { discordClient.sendSummary(this.message()) }
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
        val dailyReportHistoryCount: Long,
    ) {
        fun message(): DiscordMessageModel {
            return DiscordMessageModel(
                """
                **[ 일단위 통계 알림 ${now.format("yyyyMMdd HH:mm:ss")} ]**
                - 전날 종합 api 호출수 : $systemActionLogCount
                - 전날 종합 유저 가입수 : $userCount
                - 전날 종합 봉투 생성수 : $dailyEnvelopeCount
                - 전체 봉투 생성수 : $totalEnvelopeCount
                - 전날 종합 장부 생성수 : $dailyLedgerCount
                - 전날 종합 친구 생성수 : $friendCount
                - 전날 종합 유저 탈퇴수 : $userWithdrawCount
                - 전날 종합 신고수 : $dailyReportHistoryCount
                """.trimIndent()
            )
        }
    }
}

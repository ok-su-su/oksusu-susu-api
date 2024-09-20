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
            { withContext(Dispatchers.IO) { envelopeRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { ledgerRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { friendRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { userWithdrawRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { reportHistoryRepository.countByCreatedAtBetween(beforeOneDay, now) } },
            { withContext(Dispatchers.IO) { userRepository.countActiveUsers() } }
        ) {
                systemActionLogCount,
                userCount,
                dailyEnvelopeCount,
                dailyLedgerCount,
                friendCount,
                userWithdrawCount,
                dailyReportHistoryCount,
                totalActiveUserCount,
            ->
            DailySummaryMessage(
                now = now,
                dailySystemActionLogCount = systemActionLogCount,
                dailyUserCount = userCount,
                dailyEnvelopeCount = dailyEnvelopeCount,
                dailyLedgerCount = dailyLedgerCount,
                dailyFriendCount = friendCount,
                dailyUserWithdrawCount = userWithdrawCount,
                dailyReportHistoryCount = dailyReportHistoryCount,
                totalActiveUserCount = totalActiveUserCount
            )
        }.run { discordClient.sendSummary(this.message()) }
    }

    private data class DailySummaryMessage(
        val now: LocalDateTime,
        val dailySystemActionLogCount: Long,
        val dailyUserCount: Long,
        val dailyEnvelopeCount: Long,
        val dailyLedgerCount: Long,
        val dailyFriendCount: Long,
        val dailyUserWithdrawCount: Long,
        val dailyReportHistoryCount: Long,
        val totalActiveUserCount: Long,
    ) {
        fun message(): DiscordMessageModel {
            return DiscordMessageModel(
                """
                **[ 일단위 통계 알림 ${now.format("yyyyMMdd HH:mm:ss")} ]**
                - 전날 종합 api 호출수 : $dailySystemActionLogCount
                - 총합 실제 유저수 : $totalActiveUserCount
                - 전날 신규 가입 유저수 : $dailyUserCount
                - 전날 유저 탈퇴수 : $dailyUserWithdrawCount
                - 전날 신규 봉투 생성수 : $dailyEnvelopeCount
                - 전날 신규 장부 생성수 : $dailyLedgerCount
                - 전날 신규 친구 생성수 : $dailyFriendCount
                - 전날 종합 신고수 : $dailyReportHistoryCount
                """.trimIndent()
            )
        }
    }
}

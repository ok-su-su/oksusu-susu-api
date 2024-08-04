package com.oksusu.susu.batch.summary.job

import com.oksusu.susu.client.discord.DiscordClient
import com.oksusu.susu.client.discord.model.DiscordMessageModel
import com.oksusu.susu.common.extension.format
import com.oksusu.susu.common.extension.parZipWithMDC
import com.oksusu.susu.domain.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.domain.envelope.infrastructure.LedgerRepository
import com.oksusu.susu.domain.friend.infrastructure.FriendRepository
import com.oksusu.susu.domain.log.infrastructure.SystemActionLogRepository
import com.oksusu.susu.domain.user.infrastructure.UserRepository
import com.oksusu.susu.domain.user.infrastructure.UserWithdrawRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SusuStatisticsHourSummaryJob(
    private val discordClient: DiscordClient,
    private val systemActionLogRepository: SystemActionLogRepository,
    private val userRepository: UserRepository,
    private val envelopeRepository: EnvelopeRepository,
    private val ledgerRepository: LedgerRepository,
    private val friendRepository: FriendRepository,
    private val userWithdrawRepository: UserWithdrawRepository,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun runHourSummaryJob() {
        val now = LocalDateTime.now()
        val beforeOneHour = now.minusHours(1)

        parZipWithMDC(
            { withContext(Dispatchers.IO) { systemActionLogRepository.countByCreatedAtBetween(beforeOneHour, now) } },
            { withContext(Dispatchers.IO) { ledgerRepository.countByCreatedAtBetween(beforeOneHour, now) } },
            { withContext(Dispatchers.IO) { envelopeRepository.countByCreatedAtBetween(beforeOneHour, now) } },
            { withContext(Dispatchers.IO) { friendRepository.countByCreatedAtBetween(beforeOneHour, now) } },
            { withContext(Dispatchers.IO) { userRepository.countByCreatedAtBetween(beforeOneHour, now) } },
            { withContext(Dispatchers.IO) { userWithdrawRepository.countByCreatedAtBetween(beforeOneHour, now) } }
        ) {
                systemActionLogCount,
                ledgerCount,
                envelopeCount,
                friendCount,
                userCount,
                userWithdrawCount,
            ->
            HourSummaryMessage(
                beforeOneHour = beforeOneHour,
                now = now,
                systemActionLogCount = systemActionLogCount,
                ledgerCount = ledgerCount,
                envelopeCount = envelopeCount,
                friendCount = friendCount,
                userCount = userCount,
                userWithdrawCount = userWithdrawCount
            )
        }.run { discordClient.sendSummary(this.message()) }
    }

    private data class HourSummaryMessage(
        val beforeOneHour: LocalDateTime,
        val now: LocalDateTime,
        val systemActionLogCount: Long,
        val ledgerCount: Long,
        val envelopeCount: Long,
        val friendCount: Long,
        val userCount: Long,
        val userWithdrawCount: Long,
    ) {
        fun message(): DiscordMessageModel {
            return DiscordMessageModel(
                """
                **[ 시간단위 통계 알림 ${now.format("yyyy-MM-dd HH:mm:ss")} ]**
                - ${beforeOneHour.format("HH:mm:ss")} ~ ${now.format("HH:mm:ss")}
                - api 호출수 : $systemActionLogCount
                - 장부 생성수 : $ledgerCount
                - 봉투 생성수 : $envelopeCount
                - 친구 생성수 : $friendCount
                - 유저 생성수 : $userCount
                - 유저 탈퇴수 : $userWithdrawCount
                """.trimIndent()
            )
        }
    }
}

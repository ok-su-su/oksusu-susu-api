package com.oksusu.susu.api.event.model

import com.oksusu.susu.common.extension.mapper
import com.oksusu.susu.common.extension.remoteIp
import com.oksusu.susu.domain.envelope.domain.Ledger
import com.oksusu.susu.domain.statistic.domain.UserEnvelopeStatistic
import com.oksusu.susu.domain.term.domain.TermAgreement
import com.oksusu.susu.domain.term.domain.vo.TermAgreementChangeType
import com.oksusu.susu.domain.user.domain.UserDevice
import com.oksusu.susu.domain.user.domain.UserStatusHistory
import com.oksusu.susu.domain.user.domain.UserWithdraw
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import java.time.LocalDateTime

sealed interface Event

open class BaseEvent(
    val publishAt: LocalDateTime = LocalDateTime.now(),
) : Event

data class DeleteLedgerEvent(
    val ledger: Ledger,
) : BaseEvent()

data class TermAgreementHistoryCreateEvent(
    val termAgreements: List<TermAgreement>,
    val changeType: TermAgreementChangeType,
) : BaseEvent()

data class CreateUserDeviceEvent(
    val userDevice: UserDevice,
) : BaseEvent()

data class UpdateUserDeviceEvent(
    val userDevice: UserDevice,
) : BaseEvent()

data class SystemActionLogEvent(
    val ipAddress: String?,
    val method: String?,
    val path: String?,
    val userAgent: String?,
    val host: String?,
    val referer: String?,
    val extra: String?,
) : BaseEvent() {
    companion object {
        private const val USER_AGENT = "User-Agent"
        private const val HOST = "Host"
        private const val REFERER = "Referer"

        fun from(exchange: ServerWebExchange): SystemActionLogEvent {
            val request = exchange.request

            return SystemActionLogEvent(
                ipAddress = request.remoteIp,
                method = request.method.name(),
                path = request.uri.path,
                userAgent = request.headers[USER_AGENT].toString(),
                host = request.headers[HOST].toString(),
                referer = request.headers[REFERER].toString(),
                extra = mapper.writeValueAsString(request.headers)
            )
        }
    }
}

data class SlackErrorAlarmEvent(
    val request: ServerHttpRequest,
    val exception: Exception,
) : BaseEvent()

data class CreateUserStatusHistoryEvent(
    val userStatusHistory: UserStatusHistory,
) : BaseEvent()

data class SentryCaptureExceptionEvent(
    val request: ServerHttpRequest,
    val exception: Exception,
) : BaseEvent()

data class DeleteVoteCountEvent(
    val postId: Long,
    val optionIds: List<Long>,
) : BaseEvent()

data class CacheUserEnvelopeStatisticEvent(
    val uid: Long,
    val statistic: UserEnvelopeStatistic,
) : BaseEvent()

data class DeleteEnvelopeEvent(
    val envelopeId: Long,
    val uid: Long,
    val friendId: Long,
) : BaseEvent()

data class CreateUserWithdrawEvent(
    val userWithdraw: UserWithdraw,
) : BaseEvent()

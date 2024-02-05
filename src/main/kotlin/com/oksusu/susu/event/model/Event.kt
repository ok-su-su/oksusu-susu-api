package com.oksusu.susu.event.model

import com.oksusu.susu.envelope.domain.Ledger
import com.oksusu.susu.extension.mapper
import com.oksusu.susu.extension.remoteIp
import com.oksusu.susu.statistic.domain.UserEnvelopeStatistic
import com.oksusu.susu.term.domain.TermAgreement
import com.oksusu.susu.term.domain.vo.TermAgreementChangeType
import com.oksusu.susu.user.domain.UserDevice
import com.oksusu.susu.user.domain.UserStatusHistory
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

package com.oksusu.susu.api.envelope.model

import com.oksusu.susu.api.envelope.domain.Envelope
import com.oksusu.susu.api.envelope.domain.vo.EnvelopeType
import java.time.LocalDateTime

data class EnvelopeModel(
    /** 봉투 id */
    val id: Long,
    /** user id, 소유자 */
    val uid: Long,
    /** type: SENT, RECEIVED */
    val type: EnvelopeType,
    /** 금액 */
    val amount: Long,
    /** 선물 */
    val gift: String?,
    /** 메모 */
    val memo: String?,
    /** 방문 : 1, 미방문 : 0, null인 경우 미선택 */
    val hasVisited: Boolean?,
    /** 전달일 */
    val handedOverAt: LocalDateTime,
) {
    companion object {
        fun from(envelope: Envelope): EnvelopeModel {
            return EnvelopeModel(
                id = envelope.id,
                uid = envelope.uid,
                type = envelope.type,
                amount = envelope.amount,
                gift = envelope.gift,
                memo = envelope.memo,
                hasVisited = envelope.hasVisited,
                handedOverAt = envelope.handedOverAt
            )
        }
    }
}

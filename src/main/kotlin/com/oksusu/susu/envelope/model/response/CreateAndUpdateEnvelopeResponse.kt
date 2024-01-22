package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import java.time.LocalDateTime

data class CreateAndUpdateEnvelopeResponse(
    val id: Long = -1,
    val uid: Long,
    val type: EnvelopeType,
    val friendId: Long,
    val amount: Long,
    val gift: String?,
    val memo: String?,
    val hasVisited: Boolean?,
    val handedOverAt: LocalDateTime,
) {
    companion object {
        fun from(envelope: Envelope): CreateAndUpdateEnvelopeResponse {
            return CreateAndUpdateEnvelopeResponse(
                id = envelope.id,
                uid = envelope.uid,
                type = envelope.type,
                friendId = envelope.friendId,
                amount = envelope.amount,
                gift = envelope.gift,
                memo = envelope.memo,
                hasVisited = envelope.hasVisited,
                handedOverAt = envelope.handedOverAt
            )
        }
    }
}

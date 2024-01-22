package com.oksusu.susu.envelope.model

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import java.time.LocalDateTime

data class EnvelopeModel(
    val id: Long,
    val uid: Long,
    val type: EnvelopeType,
    val amount: Long,
    val gift: String?,
    val memo: String?,
    val hasVisited: Boolean?,
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

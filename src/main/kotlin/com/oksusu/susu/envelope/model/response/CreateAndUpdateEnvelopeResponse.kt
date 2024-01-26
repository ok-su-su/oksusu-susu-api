package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.friend.domain.Friend
import com.oksusu.susu.friend.model.FriendModel
import java.time.LocalDateTime

data class CreateAndUpdateEnvelopeResponse(
    val id: Long = -1,
    val uid: Long,
    val type: EnvelopeType,
    val amount: Long,
    val gift: String?,
    val memo: String?,
    val hasVisited: Boolean?,
    val handedOverAt: LocalDateTime,
    val friend: FriendModel,
) {
    companion object {
        fun of(envelope: Envelope, friend: Friend): CreateAndUpdateEnvelopeResponse {
            return CreateAndUpdateEnvelopeResponse(
                id = envelope.id,
                uid = envelope.uid,
                type = envelope.type,
                amount = envelope.amount,
                gift = envelope.gift,
                memo = envelope.memo,
                hasVisited = envelope.hasVisited,
                handedOverAt = envelope.handedOverAt,
                friend = FriendModel.from(friend)
            )
        }
    }
}

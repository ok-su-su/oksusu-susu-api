package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.friend.domain.Friend
import com.oksusu.susu.friend.model.FriendModel
import java.time.LocalDateTime

data class CreateAndUpdateEnvelopeResponse(
    /** 봉투 id */
    val id: Long = -1,
    /** user id */
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
    /** 지인 */
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

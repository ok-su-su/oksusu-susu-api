package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.friend.model.FriendModel

data class SearchFriendEnvelopeResponse(
    val friend: FriendModel,
    val sentAmount: Long,
    val receivedAmount: Long,
    val totalAmount: Long,
)

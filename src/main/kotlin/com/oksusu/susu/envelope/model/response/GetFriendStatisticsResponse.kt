package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.friend.domain.Friend

data class GetFriendStatisticsResponse(
    val friend: Friend,
    val totalAmounts: Long,
    val sentAmounts: Long,
    val receivedAmounts: Long,
)

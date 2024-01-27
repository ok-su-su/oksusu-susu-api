package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.friend.model.FriendModel

data class GetFriendStatisticsResponse(
    /** 지인 */
    val friend: FriendModel,
    /** 받은, 보낸 금액 총합 */
    val totalAmounts: Long,
    /** 보낸 금액 총합 */
    val sentAmounts: Long,
    /** 받은 금액 총합 */
    val receivedAmounts: Long,
)

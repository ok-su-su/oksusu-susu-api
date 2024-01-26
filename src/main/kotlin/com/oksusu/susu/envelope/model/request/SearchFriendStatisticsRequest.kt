package com.oksusu.susu.envelope.model.request

data class SearchFriendStatisticsRequest(
    /** 지인 ids */
    val friendIds: Set<Long>?,
    /** 금액 총합 from */
    val fromTotalAmounts: Long?,
    /** 금액 총합 to */
    val toTotalAmounts: Long?,
)

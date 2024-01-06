package com.oksusu.susu.envelope.model.request

data class SearchFriendStatisticsRequest(
    val friendIds: Set<Long>?,
    val fromTotalAmounts: Long?,
    val toTotalAmounts: Long?,
)

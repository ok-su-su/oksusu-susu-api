package com.oksusu.susu.domain.envelope.infrastructure.model

data class SearchFriendStatisticsSpec(
    val uid: Long,
    val friendIds: Set<Long>?,
    val fromTotalAmounts: Long?,
    val toTotalAmounts: Long?,
)

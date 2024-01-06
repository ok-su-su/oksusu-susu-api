package com.oksusu.susu.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class SearchFriendStatisticsSpec @QueryProjection constructor(
    val uid: Long,
    val friendIds: List<Long>?,
    val fromTotalAmounts: Long?,
    val toTotalAmounts: Long?,
)

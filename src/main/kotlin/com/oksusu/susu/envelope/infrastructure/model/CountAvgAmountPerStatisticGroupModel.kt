package com.oksusu.susu.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class CountAvgAmountPerStatisticGroupModel @QueryProjection constructor(
    val categoryId: Long,
    val relationshipId: Long,
    val birth: Long,
    val totalAmounts: Long,
    val counts: Long,
)

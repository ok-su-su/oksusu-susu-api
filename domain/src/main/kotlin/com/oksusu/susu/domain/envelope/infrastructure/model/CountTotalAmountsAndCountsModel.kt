package com.oksusu.susu.domain.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class CountTotalAmountsAndCountsModel @QueryProjection constructor(
    val ledgerId: Long,
    val totalAmounts: Long,
    val totalCounts: Long,
)

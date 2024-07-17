package com.oksusu.susu.domain.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class CountTotalAmountsAndCountsModel @QueryProjection constructor(
    val ledgerId: Long,
    val totalSentAmounts: Long,
    val totalReceivedAmounts: Long,
    val totalCounts: Long,
)

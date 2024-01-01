package com.oksusu.susu.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class CountPerHandedOverAtModel @QueryProjection constructor(
    val handedOverAtMonth: Int,
    val totalCounts: Long,
)

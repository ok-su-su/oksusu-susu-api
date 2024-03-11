package com.oksusu.susu.api.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class CountPerHandedOverAtModel @QueryProjection constructor(
    val handedOverAtMonth: Int,
    val totalAmounts: Long,
)

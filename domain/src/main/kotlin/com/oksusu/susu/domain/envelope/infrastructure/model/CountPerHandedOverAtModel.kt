package com.oksusu.susu.domain.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class CountPerHandedOverAtModel @QueryProjection constructor(
    val handedOverAtMonth: Int,
    val totalAmounts: Long,
)

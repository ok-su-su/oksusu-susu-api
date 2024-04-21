package com.oksusu.susu.domain.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class CountPerCategoryIdModel @QueryProjection constructor(
    val categoryId: Long,
    val totalCounts: Long,
)

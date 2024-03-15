package com.oksusu.susu.domain.friend.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class CountPerRelationshipIdModel @QueryProjection constructor(
    val relationshipId: Long,
    val totalCounts: Long,
)

package com.oksusu.susu.friend.infrastructure.model

import com.oksusu.susu.friend.domain.Relationship
import com.querydsl.core.annotations.QueryProjection

data class CountPerRelationshipIdModel @QueryProjection constructor(
    val relationship: Relationship,
    val totalCounts: Long,
)

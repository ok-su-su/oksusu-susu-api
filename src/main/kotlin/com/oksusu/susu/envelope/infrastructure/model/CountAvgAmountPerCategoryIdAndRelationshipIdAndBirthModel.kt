package com.oksusu.susu.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class CountAvgAmountPerCategoryIdAndRelationshipIdAndBirthModel @QueryProjection constructor(
    val categoryId: Long,
    val relationshipId: Long,
    val birth: Long,
    val averageAmount: Long,
)

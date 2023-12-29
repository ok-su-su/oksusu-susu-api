package com.oksusu.susu.statistic.model

class SusuSpecificStatisticModel(
    // 평균 보낸 비용
    val averageSent: String?,
    // 관계 별 평균
    val averageRelationship: TitleStringModel?,
    // 카테고리 별 평균
    val averageCategory: TitleStringModel?,
)

package com.oksusu.susu.api.statistic.model

data class SusuSpecificEnvelopeStatisticModel(
    /** 평균 보낸 비용 */
    val averageSent: Long?,
    /** 관계 별 평균 */
    val averageRelationship: TitleValueModel<Long>?,
    /** 카테고리 별 평균 */
    val averageCategory: TitleValueModel<Long>?,
)

package com.oksusu.susu.api.statistic.model

import com.oksusu.susu.common.model.TitleValueModel

data class SusuEnvelopeStatisticModel(
    /** 최근 사용 금액 */
    val recentSpent: List<TitleValueModel<Long>>?,
    /** 경조사비를 가장 많이 쓴 달 */
    val mostSpentMonth: Long?,
    /** 최다 수수 관계 */
    val relationship: TitleValueModel<Long>?,
    /** 최다 수수 경조사 */
    val category: TitleValueModel<Long>?,
)

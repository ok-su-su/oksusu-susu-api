package com.oksusu.susu.domain.statistic.domain

import com.oksusu.susu.common.model.TitleValueModel

class SusuEnvelopeStatistic(
    /** 최근 사용 금액 */
    val recentSpent: List<TitleValueModel<Long>>?,
    /** 경조사비를 가장 많이 쓴 달 */
    val mostSpentMonth: Long?,
    /** 최다 수수 관계 */
    val mostFrequentRelationShip: TitleValueModel<Long>?,
    /** 최다 수수 경조사 */
    val mostFrequentCategory: TitleValueModel<Long>?,
)

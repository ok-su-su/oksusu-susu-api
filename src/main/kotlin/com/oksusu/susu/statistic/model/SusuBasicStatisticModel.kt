package com.oksusu.susu.statistic.model

class SusuBasicStatisticModel(
    /** 최근 사용 금액 */
    val recentSpent: List<TitleValueModel>?,
    /** 경조사비를 가장 많이 쓴 달 */
    val mostSpentMonth: Long?,
    /** 최다 수수 관계 */
    val relationship: TitleValueModel?,
    /** 최다 수수 경조사 */
    val category: TitleValueModel?,
)

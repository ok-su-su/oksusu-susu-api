package com.oksusu.susu.statistic.domain

import com.oksusu.susu.statistic.model.TitleValueModel
import com.oksusu.susu.statistic.model.response.UserStatisticResponse

class UserStatistic(
    // 최근 사용 금액
    val recentSpent: List<TitleValueModel>?,
    // 경조사비를 가장 많이 쓴 달
    val mostSpentMonth: Long?,
    // 최다 수수 관계
    val mostRelationship: TitleValueModel?,
    // 최다 수수 경조사
    val mostCategory: TitleValueModel?,
    // 가장 많이 받은 금액
    val highestAmountReceived: TitleValueModel?,
    // 가장 많이 보낸 금액
    val highestAmountSent: TitleValueModel?,
) {
    companion object {
        fun from(statistic: UserStatisticResponse): UserStatistic {
            return UserStatistic(
                recentSpent = statistic.recentSpent,
                mostSpentMonth = statistic.mostSpentMonth,
                mostRelationship = statistic.mostRelationship,
                mostCategory = statistic.mostCategory,
                highestAmountReceived = statistic.highestAmountReceived,
                highestAmountSent = statistic.highestAmountSent
            )
        }
    }
}

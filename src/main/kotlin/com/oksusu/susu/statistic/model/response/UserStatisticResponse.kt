package com.oksusu.susu.statistic.model.response

import com.oksusu.susu.statistic.domain.UserStatistic
import com.oksusu.susu.statistic.model.TitleValueModel

class UserStatisticResponse(
    // 최근 사용 금액
    val recentSpent: List<TitleValueModel>?,
    // 경조사비를 가장 많이 쓴 달
    val mostSpentMonth: Long?,
    // 최다 수수 관계
    val relationship: TitleValueModel?,
    // 최다 수수 경조사
    val category: TitleValueModel?,
    // 가장 많이 받은 금액
    val received: TitleValueModel?,
    // 가장 많이 보낸 금액
    val sent: TitleValueModel?,
) {
    companion object {
        fun from(userStatistic: UserStatistic): UserStatisticResponse {
            return userStatistic.userStatisticResponse
        }
    }
}

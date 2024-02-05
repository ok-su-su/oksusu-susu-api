package com.oksusu.susu.statistic.model.response

import com.oksusu.susu.statistic.domain.SusuEnvelopeStatistic
import com.oksusu.susu.statistic.domain.UserEnvelopeStatistic
import com.oksusu.susu.statistic.model.TitleValueModel

class UserEnvelopeStatisticResponse(
    /** 최근 사용 금액 */
    val recentSpent: List<TitleValueModel<Long>>?,
    /** 경조사비를 가장 많이 쓴 달 */
    val mostSpentMonth: Long?,
    /** 최다 수수 관계 */
    val mostRelationship: TitleValueModel<Long>?,
    /** 최다 수수 경조사 */
    val mostCategory: TitleValueModel<Long>?,
    /** 가장 많이 받은 금액 */
    val highestAmountReceived: TitleValueModel<Long>?,
    /** 가장 많이 보낸 금액 */
    val highestAmountSent: TitleValueModel<Long>?,
) {
    companion object {
        fun from(userEnvelopeStatistic: UserEnvelopeStatistic): UserEnvelopeStatisticResponse {
            return UserEnvelopeStatisticResponse(
                recentSpent = userEnvelopeStatistic.recentSpent,
                mostSpentMonth = userEnvelopeStatistic.mostSpentMonth,
                mostRelationship = userEnvelopeStatistic.mostFrequentRelationShip,
                mostCategory = userEnvelopeStatistic.mostFrequentCategory,
                highestAmountReceived = userEnvelopeStatistic.maxReceivedEnvelope,
                highestAmountSent = userEnvelopeStatistic.maxSentEnvelope
            )
        }
    }
}

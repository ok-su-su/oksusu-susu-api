package com.oksusu.susu.api.statistic.model.response

import com.oksusu.susu.domain.statistic.domain.SusuEnvelopeStatistic
import com.oksusu.susu.api.statistic.model.SusuSpecificEnvelopeStatisticModel
import com.oksusu.susu.domain.statistic.domain.vo.TitleValueModel

data class SusuEnvelopeStatisticResponse(
    /** 평균 보낸 비용 */
    val averageSent: Long?,
    /** 관계 별 평균 */
    val averageRelationship: TitleValueModel<Long>?,
    /** 카테고리 별 평균 */
    val averageCategory: TitleValueModel<Long>?,
    /** 최근 사용 금액 */
    val recentSpent: List<TitleValueModel<Long>>?,
    /** 경조사비를 가장 많이 쓴 달 */
    val mostSpentMonth: Long?,
    /** 최다 수수 관계 */
    val mostRelationship: TitleValueModel<Long>?,
    /** 최다 수수 경조사 */
    val mostCategory: TitleValueModel<Long>?,
) {
    companion object {
        fun of(
            specific: SusuSpecificEnvelopeStatisticModel,
            statistic: SusuEnvelopeStatistic,
        ): SusuEnvelopeStatisticResponse {
            return SusuEnvelopeStatisticResponse(
                averageSent = specific.averageSent,
                averageRelationship = specific.averageRelationship,
                averageCategory = specific.averageCategory,
                recentSpent = statistic.recentSpent,
                mostSpentMonth = statistic.mostSpentMonth,
                mostRelationship = statistic.mostFrequentRelationShip,
                mostCategory = statistic.mostFrequentCategory
            )
        }
    }
}

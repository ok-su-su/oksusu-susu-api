package com.oksusu.susu.statistic.model.response

import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import com.oksusu.susu.statistic.model.SusuSpecificStatisticModel
import com.oksusu.susu.statistic.model.TitleStringModel
import com.oksusu.susu.statistic.model.TitleValueModel

data class SusuStatisticResponse(
    /** 평균 보낸 비용 */
    val averageSent: String?,
    /** 관계 별 평균 */
    val averageRelationship: TitleStringModel?,
    /** 카테고리 별 평균 */
    val averageCategory: TitleStringModel?,
    /** 최근 사용 금액 */
    val recentSpent: List<TitleValueModel>?,
    /** 경조사비를 가장 많이 쓴 달 */
    val mostSpentMonth: Long?,
    /** 최다 수수 관계 */
    val mostRelationship: TitleValueModel?,
    /** 최다 수수 경조사 */
    val mostCategory: TitleValueModel?,
) {
    companion object {
        fun of(specific: SusuSpecificStatisticModel, basic: SusuBasicStatistic): SusuStatisticResponse {
            return SusuStatisticResponse(
                averageSent = specific.averageSent,
                averageRelationship = specific.averageRelationship,
                averageCategory = specific.averageCategory,
                recentSpent = basic.recentSpent,
                mostSpentMonth = basic.mostSpentMonth,
                mostRelationship = basic.relationship,
                mostCategory = basic.category
            )
        }
    }
}

package com.oksusu.susu.cache.model

import com.oksusu.susu.common.model.TitleValueModel

class UserEnvelopeStatisticCacheModel(
    /** 최근 사용 금액 */
    val recentSpent: List<TitleValueModel<Long>>?,
    /** 경조사비를 가장 많이 쓴 달 */
    val mostSpentMonth: Long?,
    /** 최다 수수 관계 */
    val mostFrequentRelationShip: TitleValueModel<Long>?,
    /** 최다 수수 경조사 */
    val mostFrequentCategory: TitleValueModel<Long>?,
    /** 가장 많이 받은 금액 */
    val maxReceivedEnvelope: TitleValueModel<Long>?,
    /** 가장 많이 보낸 금액 */
    val maxSentEnvelope: TitleValueModel<Long>?,
)

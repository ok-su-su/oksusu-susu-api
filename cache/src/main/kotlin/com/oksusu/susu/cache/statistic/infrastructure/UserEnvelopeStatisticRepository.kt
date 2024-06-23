package com.oksusu.susu.cache.statistic.infrastructure

import com.oksusu.susu.cache.statistic.domain.UserEnvelopeStatistic

interface UserEnvelopeStatisticRepository {
    suspend fun save(uid: Long, value: UserEnvelopeStatistic)

    suspend fun getStatistic(uid: Long): UserEnvelopeStatistic?
}

package com.oksusu.susu.cache.statistic.infrastructure

import com.oksusu.susu.cache.statistic.domain.SusuEnvelopeStatistic

interface SusuEnvelopeStatisticRepository {
    suspend fun save(value: SusuEnvelopeStatistic)

    suspend fun getStatistic(): SusuEnvelopeStatistic?
}

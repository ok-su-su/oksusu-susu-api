package com.oksusu.susu.cache.statistic.infrastructure

interface SusuSpecificEnvelopeStatisticRepository {
    suspend fun findByKey(key: String): Long?

    suspend fun save(key: String, value: Long)
}


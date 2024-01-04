package com.oksusu.susu.cache

interface KeyValueCacheService {
    suspend fun save(key: String, value: String, ttl: Int)
    suspend fun save(key: String, value: String)
    suspend fun findByKey(key: String): String?
}

package com.oksusu.susu.cache

interface CacheService : ZSetCacheService, KeyValueCacheService {
    suspend fun <VALUE_TYPE : Any> set(cache: Cache<VALUE_TYPE>, value: VALUE_TYPE)
    suspend fun <VALUE_TYPE : Any> getOrNull(cache: Cache<VALUE_TYPE>, valueType: VALUE_TYPE): VALUE_TYPE?
}

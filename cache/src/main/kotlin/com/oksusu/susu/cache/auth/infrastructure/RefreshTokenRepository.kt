package com.oksusu.susu.cache.auth.infrastructure

import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.cache.auth.domain.RefreshToken
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepository(
    private val cacheService: CacheService,
) {
    suspend fun save(value: RefreshToken, ttl: Long) {
        cacheService.set(
            cache = getCache(value.uid.toString(), ttl),
            value = value.refreshToken
        )
    }

    suspend fun deleteByKey(key: String) {
        cacheService.delete(cache = getCache(key, 0))
    }

    private fun getCache(key: String, ttl: Long): Cache<String> {
        return Cache.getRefreshTokenCache(key = key, ttl = ttl)
    }
}

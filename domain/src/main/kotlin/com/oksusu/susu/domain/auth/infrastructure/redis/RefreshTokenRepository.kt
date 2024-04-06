package com.oksusu.susu.domain.auth.infrastructure.redis

import com.oksusu.susu.domain.auth.domain.RefreshToken
import com.oksusu.susu.cache.Cache
import com.oksusu.susu.cache.CacheService
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

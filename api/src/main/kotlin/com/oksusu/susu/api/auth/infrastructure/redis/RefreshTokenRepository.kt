package com.oksusu.susu.api.auth.infrastructure.redis

import com.oksusu.susu.api.auth.domain.RefreshToken
import com.oksusu.susu.api.cache.Cache
import com.oksusu.susu.api.cache.CacheService
import com.oksusu.susu.api.config.jwt.JwtConfig
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepository(
    private val cacheService: CacheService,
    private val jwtConfig: JwtConfig,
) {
    suspend fun save(value: RefreshToken) {
        cacheService.set(
            cache = getCache(value.uid.toString()),
            value = value.refreshToken
        )
    }

    suspend fun deleteByKey(key: String) {
        cacheService.delete(cache = getCache(key))
    }

    private fun getCache(key: String): Cache<String> {
        return Cache.getRefreshTokenCache(key = key, ttl = jwtConfig.refreshExp.toLong())
    }
}

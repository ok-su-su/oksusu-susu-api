package com.oksusu.susu.cache.auth.infrastructure

import com.oksusu.susu.cache.auth.domain.RefreshToken

interface RefreshTokenRepository {
    suspend fun save(value: RefreshToken, ttl: Long)

    suspend fun deleteByKey(key: String)
}

package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.infrastructure.redis.RefreshTokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    suspend fun deleteByKey(key: String) {
        refreshTokenRepository.deleteByKey(key)
    }

    suspend fun save(token: RefreshToken) {
        withContext(Dispatchers.IO) {
            refreshTokenRepository.save(token)
        }
    }
}

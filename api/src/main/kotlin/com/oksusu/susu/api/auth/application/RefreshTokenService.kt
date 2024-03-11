package com.oksusu.susu.api.auth.application

import com.oksusu.susu.api.auth.domain.RefreshToken
import com.oksusu.susu.api.auth.infrastructure.redis.RefreshTokenRepository
import com.oksusu.susu.api.extension.withMDCContext
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    suspend fun deleteByKey(key: String) {
        refreshTokenRepository.deleteByKey(key)
    }

    suspend fun save(token: RefreshToken) {
        withMDCContext(Dispatchers.IO) {
            refreshTokenRepository.save(token)
        }
    }
}

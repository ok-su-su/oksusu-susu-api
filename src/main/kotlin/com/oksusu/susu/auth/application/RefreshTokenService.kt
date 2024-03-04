package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.infrastructure.redis.RefreshTokenRepository
import com.oksusu.susu.extension.withMDCContext
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

package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.infrastructure.redis.RefreshTokenRepository
import com.oksusu.susu.extension.withMDCContext
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
        withContext(Dispatchers.IO.withMDCContext()) {
            refreshTokenRepository.save(token)
        }
    }
}

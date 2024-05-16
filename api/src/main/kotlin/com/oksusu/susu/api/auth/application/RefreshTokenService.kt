package com.oksusu.susu.api.auth.application

import com.oksusu.susu.api.config.jwt.JwtConfig
import com.oksusu.susu.cache.auth.domain.RefreshToken
import com.oksusu.susu.cache.auth.infrastructure.RefreshTokenRepository
import com.oksusu.susu.common.extension.withMDCContext
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtConfig: JwtConfig,
) {
    suspend fun deleteByKey(key: String) {
        refreshTokenRepository.deleteByKey(key)
    }

    suspend fun save(token: RefreshToken) {
        withMDCContext(Dispatchers.IO) {
            refreshTokenRepository.save(token, jwtConfig.refreshExp.toLong())
        }
    }
}

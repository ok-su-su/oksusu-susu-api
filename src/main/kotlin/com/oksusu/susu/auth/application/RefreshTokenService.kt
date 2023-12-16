package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.infrastructure.repository.RefreshTokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    suspend fun deleteById(id: Long) {
        withContext(Dispatchers.IO) {
            refreshTokenRepository.deleteById(id)
        }
    }

    suspend fun save(token: RefreshToken): RefreshToken {
        return withContext(Dispatchers.IO) {
            refreshTokenRepository.save(token)
        }
    }
}

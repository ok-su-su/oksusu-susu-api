package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.infrastructure.repository.RefreshTokenRepository
import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun deleteByIdSync(id: Long) {
        refreshTokenRepository.deleteById(id)
    }

    fun saveSync(token: RefreshToken): RefreshToken {
        return refreshTokenRepository.save(token)
    }
}

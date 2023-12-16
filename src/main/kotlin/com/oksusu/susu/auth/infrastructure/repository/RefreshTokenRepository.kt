package com.oksusu.susu.auth.infrastructure.repository

import com.oksusu.susu.auth.domain.RefreshToken
import org.springframework.data.repository.CrudRepository

interface RefreshTokenRepository : CrudRepository<RefreshToken, Long> {
    fun findByRefreshToken(refreshToken: String): RefreshToken?
}

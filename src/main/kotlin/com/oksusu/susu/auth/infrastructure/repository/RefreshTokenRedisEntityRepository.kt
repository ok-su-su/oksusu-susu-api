package com.oksusu.susu.auth.infrastructure.repository

import com.oksusu.susu.auth.model.RefreshTokenRedisEntity
import org.springframework.data.repository.CrudRepository

interface RefreshTokenRedisEntityRepository : CrudRepository<RefreshTokenRedisEntity, Long> {
    fun findByRefreshToken(refreshToken: String): RefreshTokenRedisEntity?
}

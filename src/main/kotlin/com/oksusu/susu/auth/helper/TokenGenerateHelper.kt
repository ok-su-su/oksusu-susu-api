package com.oksusu.susu.auth.helper

import com.oksusu.susu.auth.application.JwtTokenService
import com.oksusu.susu.auth.model.dto.TokenDto
import com.oksusu.susu.config.jwt.JwtConfig
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TokenGenerateHelper(
    private val jwtTokenService: JwtTokenService,
    private val jwtConfig: JwtConfig,
) {
    fun generateAccessAndRefreshToken(id: Long): TokenDto {
        val issuedAt = LocalDateTime.now()
        val accessTokenExpiresIn = issuedAt.plusSeconds(jwtConfig.accessExp.toLong())
        val accessToken = jwtTokenService.createToken(id, accessTokenExpiresIn)

        val refreshTokenExpiresIn = issuedAt.plusSeconds(jwtConfig.refreshExp.toLong())
        val refreshToken = jwtTokenService.createRefreshToken(id, refreshTokenExpiresIn)

        return TokenDto(
            accessToken = accessToken,
            accessTokenExp = accessTokenExpiresIn,
            refreshToken = refreshToken,
            refreshTokenExp = refreshTokenExpiresIn,
        )
    }

    fun getRefreshTokenTtlSecond(): Int = jwtConfig.refreshExp
}

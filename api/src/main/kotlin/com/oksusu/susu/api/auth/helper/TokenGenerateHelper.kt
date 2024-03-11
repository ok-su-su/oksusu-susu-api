package com.oksusu.susu.api.auth.helper

import com.oksusu.susu.api.auth.application.JwtTokenService
import com.oksusu.susu.api.auth.model.TokenDto
import com.oksusu.susu.common.config.jwt.JwtConfig
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TokenGenerateHelper(
    private val jwtTokenService: JwtTokenService,
    private val jwtConfig: com.oksusu.susu.common.config.jwt.JwtConfig,
) {
    fun generateAccessAndRefreshToken(uid: Long): TokenDto {
        val issuedAt = LocalDateTime.now()
        val accessTokenExpiresIn = issuedAt.plusSeconds(jwtConfig.accessExp.toLong())
        val accessToken = jwtTokenService.createToken(uid, accessTokenExpiresIn)

        val refreshTokenExpiresIn = issuedAt.plusSeconds(jwtConfig.refreshExp.toLong())
        val refreshToken = jwtTokenService.createRefreshToken(uid, refreshTokenExpiresIn)

        return TokenDto(
            accessToken = accessToken,
            accessTokenExp = accessTokenExpiresIn,
            refreshToken = refreshToken,
            refreshTokenExp = refreshTokenExpiresIn
        )
    }
}

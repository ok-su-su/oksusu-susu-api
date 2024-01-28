package com.oksusu.susu.auth.model

import java.time.LocalDateTime

/** 토큰 정보 dto */
class TokenDto(
    /** access token */
    val accessToken: String,
    /** access token 유효기간 */
    val accessTokenExp: LocalDateTime,
    /** refresh token */
    val refreshToken: String,
    /** refresh token 유효기간 */
    val refreshTokenExp: LocalDateTime,
)

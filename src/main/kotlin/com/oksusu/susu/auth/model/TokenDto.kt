package com.oksusu.susu.auth.model

import java.time.LocalDateTime

class TokenDto(
    val accessToken: String,
    val accessTokenExp: LocalDateTime,
    val refreshToken: String,
    val refreshTokenExp: LocalDateTime,
)

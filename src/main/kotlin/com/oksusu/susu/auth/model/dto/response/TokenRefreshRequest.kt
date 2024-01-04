package com.oksusu.susu.auth.model.dto.response

data class TokenRefreshRequest(
    val accessToken: String,
    val refreshToken: String,
)

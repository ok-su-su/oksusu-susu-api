package com.oksusu.susu.auth.model.response

data class TokenRefreshRequest(
    val accessToken: String,
    val refreshToken: String,
)

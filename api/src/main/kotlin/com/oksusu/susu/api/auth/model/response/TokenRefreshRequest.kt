package com.oksusu.susu.api.auth.model.response

data class TokenRefreshRequest(
    /** susu access token */
    val accessToken: String,
    /** susu refresh token */
    val refreshToken: String,
)

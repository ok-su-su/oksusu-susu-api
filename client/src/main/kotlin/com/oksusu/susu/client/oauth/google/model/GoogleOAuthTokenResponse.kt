package com.oksusu.susu.client.oauth.google.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleOAuthTokenResponse(
    /**
     * Google accessToken
     */
    val accessToken: String,
    /**
     * Google refreshToken
     */
    val refreshToken: String,
    /**
     * Google accessToken 유효기간
     */
    val expiredAt: Long,
    /**
     * Google accessToken scope
     */
    val scope: String,
    /**
     * Google accessToken type
     */
    val tokenType: String,
    /**
     * Google idToken
     */
    val idToken: String,
)

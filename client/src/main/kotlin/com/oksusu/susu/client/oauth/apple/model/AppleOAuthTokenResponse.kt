package com.oksusu.susu.client.oauth.apple.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AppleOAuthTokenResponse(
    /**
     * Apple accessToken
     */
    val accessToken: String,
    /**
     * Apple refreshToken
     */
    val refreshToken: String,
    /**
     * Apple idToken
     */
    val idToken: String,
)

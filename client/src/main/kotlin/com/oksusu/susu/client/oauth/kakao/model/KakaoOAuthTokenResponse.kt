package com.oksusu.susu.client.oauth.kakao.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoOAuthTokenResponse(
    /** 카카오 access token */
    val accessToken: String,
    /** 카카오 refresh token */
    val refreshToken: String,
)

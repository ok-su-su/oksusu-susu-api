package com.oksusu.susu.auth.infrastructure.oauth.kakao.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class KakaoOauthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
)

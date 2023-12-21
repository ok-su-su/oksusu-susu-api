package com.oksusu.susu.client.oauth.kakao.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class KakaoOauthWithdrawResponse(
    val id: String,
)

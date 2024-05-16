package com.oksusu.susu.client.oauth.kakao.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoOAuthWithdrawResponse(
    /** 카카오 회원 탈퇴 유저 id */
    val id: String,
)

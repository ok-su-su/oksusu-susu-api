package com.oksusu.susu.client.oauth.kakao.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoOAuthWithdrawRequest(
    /** 카카오 회원 탈퇴 타입 */
    val targetIdType: String = "user_id",
    /** 카카오 회원 탈퇴 id */
    val targetId: String,
)

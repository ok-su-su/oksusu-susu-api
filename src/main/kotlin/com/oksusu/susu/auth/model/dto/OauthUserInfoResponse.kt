package com.oksusu.susu.auth.model.dto

import com.oksusu.susu.auth.infrastructure.oauth.kakao.dto.KakaoOauthUserInfoResponse

class OauthUserInfoResponse(
    val oid: String,
) {
    companion object {
        fun fromKakao(kakaoOauthUserInfoResponse: KakaoOauthUserInfoResponse): OauthUserInfoResponse {
            return OauthUserInfoResponse(
                oid = kakaoOauthUserInfoResponse.id
            )
        }
    }
}


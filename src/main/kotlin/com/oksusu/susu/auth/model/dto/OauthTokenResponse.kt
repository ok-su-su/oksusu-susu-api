package com.oksusu.susu.auth.model.dto

import com.oksusu.susu.auth.infrastructure.oauth.kakao.dto.KakaoOauthTokenResponse

class OauthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun fromKakao(kakaoOauthTokenResponse: KakaoOauthTokenResponse): OauthTokenResponse {
            return OauthTokenResponse(
                accessToken = kakaoOauthTokenResponse.accessToken,
                refreshToken = kakaoOauthTokenResponse.refreshToken
            )
        }
    }
}

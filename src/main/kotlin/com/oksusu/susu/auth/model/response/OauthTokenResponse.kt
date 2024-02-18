package com.oksusu.susu.auth.model.response

import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthTokenResponse

class OAuthTokenResponse(
    /** oauth access token */
    val accessToken: String,
    /** oauth refresh token */
    val refreshToken: String,
) {
    companion object {
        fun fromKakao(kakaoOAuthTokenResponse: KakaoOAuthTokenResponse): OAuthTokenResponse {
            return OAuthTokenResponse(
                accessToken = kakaoOAuthTokenResponse.accessToken,
                refreshToken = kakaoOAuthTokenResponse.refreshToken
            )
        }
    }
}

package com.oksusu.susu.api.auth.model.response

import com.oksusu.susu.client.oauth.apple.model.AppleOAuthTokenResponse
import com.oksusu.susu.client.oauth.google.model.GoogleOAuthTokenResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthTokenResponse

data class OAuthTokenResponse(
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

        fun fromApple(appleOAuthTokenResponse: AppleOAuthTokenResponse): OAuthTokenResponse {
            return OAuthTokenResponse(
                accessToken = appleOAuthTokenResponse.accessToken,
                refreshToken = appleOAuthTokenResponse.refreshToken
            )
        }

        fun fromGoogle(googleOAuthTokenResponse: GoogleOAuthTokenResponse): OAuthTokenResponse {
            return OAuthTokenResponse(
                accessToken = googleOAuthTokenResponse.accessToken,
                refreshToken = googleOAuthTokenResponse.refreshToken
            )
        }
    }
}

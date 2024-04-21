package com.oksusu.susu.api.auth.model

import com.oksusu.susu.client.oauth.google.model.GoogleOAuthUserInfoResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthUserInfoResponse
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import com.oksusu.susu.domain.user.domain.vo.OauthInfo

/** oauth 정보 dto */
class OAuthUserInfoDto(
    /** oauth 정보 */
    val oauthInfo: OauthInfo,
) {
    companion object {
        fun fromKakao(kakaoOAuthUserInfoResponse: KakaoOAuthUserInfoResponse): OAuthUserInfoDto {
            return OAuthUserInfoDto(
                OauthInfo(
                    oAuthId = kakaoOAuthUserInfoResponse.id,
                    oAuthProvider = OAuthProvider.KAKAO
                )
            )
        }

        fun fromApple(oAuthId: String): OAuthUserInfoDto {
            return OAuthUserInfoDto(
                OauthInfo(
                    oAuthProvider = OAuthProvider.APPLE,
                    oAuthId = oAuthId
                )
            )
        }

        fun fromGoogle(googleOAuthUserInfoResponse: GoogleOAuthUserInfoResponse): OAuthUserInfoDto {
            return OAuthUserInfoDto(
                OauthInfo(
                    oAuthProvider = OAuthProvider.GOOGLE,
                    oAuthId = googleOAuthUserInfoResponse.id
                )
            )
        }
    }
}

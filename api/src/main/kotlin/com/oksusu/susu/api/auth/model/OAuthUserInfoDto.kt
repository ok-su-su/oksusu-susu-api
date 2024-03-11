package com.oksusu.susu.api.auth.model

import com.oksusu.susu.api.client.oauth.kakao.model.KakaoOAuthUserInfoResponse
import com.oksusu.susu.api.user.domain.vo.OauthInfo

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
    }
}

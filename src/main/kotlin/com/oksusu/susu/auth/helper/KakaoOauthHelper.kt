package com.oksusu.susu.auth.helper

import com.oksusu.susu.auth.model.dto.OauthLoginLinkResponse
import com.oksusu.susu.common.properties.KakaoOauthProperties
import org.springframework.stereotype.Component


const val KAKAO_OAUTH_QUERY_STRING = "/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code"

@Component
class KakaoOauthHelper(
    val kakaoOauthProperties: KakaoOauthProperties,
) {

    /** link */
    fun getOauthLoginLinkDev(): OauthLoginLinkResponse {
        return OauthLoginLinkResponse(
            kakaoOauthProperties.baseUrl
                    + java.lang.String.format(
                KAKAO_OAUTH_QUERY_STRING,
                kakaoOauthProperties.clientId,
                kakaoOauthProperties.redirectUrl
            )
        )
    }

    /** oauth token 받아오기 */
//    fun getOauthToken(code: String, referer: String): OauthTokenResponse {
//
//    }

    /** 회원 탈퇴 */
//    fun withdrawOauthUser(oid: String) {
//
//    }

    /** 유저 정보 가져오기 */
//    fun getUserInfo(oauthAccessToken: String): OauthUserInfoDto {
//
//    }


}

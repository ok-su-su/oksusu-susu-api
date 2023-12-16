package com.oksusu.susu.auth.helper

import com.oksusu.susu.auth.model.dto.OauthUserInfoDto
import com.oksusu.susu.auth.model.dto.response.AbleRegisterResponse
import com.oksusu.susu.auth.model.dto.response.OauthLoginLinkResponse
import com.oksusu.susu.auth.model.dto.response.OauthTokenResponse
import com.oksusu.susu.client.oauth.kakao.KakaoClient
import com.oksusu.susu.common.properties.KakaoOauthProperties
import com.oksusu.susu.user.application.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

private const val KAKAO_OAUTH_QUERY_STRING = "/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code"
private const val KAKAO_KAUTH_URL = "https://kauth.kakao.com"

@Component
class KakaoOauthHelper(
    val kakaoOauthProperties: KakaoOauthProperties,
    val kakaoClient: KakaoClient,
    private val userService: UserService,
) {
    private val logger = mu.KotlinLogging.logger { }

    /** link */
    suspend fun getOauthLoginLinkDev(): OauthLoginLinkResponse = OauthLoginLinkResponse(
        KAKAO_KAUTH_URL +
            String.format(
                KAKAO_OAUTH_QUERY_STRING,
                kakaoOauthProperties.clientId,
                kakaoOauthProperties.redirectUrl
            )
    )

    /** oauth token 받아오기 */
    suspend fun getOauthTokenDev(code: String): OauthTokenResponse {
        val response = kakaoClient.kakaoTokenClient(kakaoOauthProperties.redirectUrl, code)
        return OauthTokenResponse.fromKakao(response)
    }

    /** 회원가입 가능 여부 체크. */
    suspend fun checkRegisterValid(accessToken: String): AbleRegisterResponse = withContext(Dispatchers.IO) {
        val userInfoDeferred = getKakaoUserInfo(accessToken)
        val oauthInfo = userInfoDeferred.oauthInfo
        val canRegisterDeferred = userService.existsByOauthInfo(oauthInfo)

        AbleRegisterResponse(!canRegisterDeferred)
    }

    /** 유저 정보를 가져옵니다. */
    suspend fun getKakaoUserInfo(accessToken: String): OauthUserInfoDto {
        return withContext(Dispatchers.IO) {
            kakaoClient.kakaoUserInfoClient(accessToken)
        }.run { OauthUserInfoDto.fromKakao(this) }
    }
}

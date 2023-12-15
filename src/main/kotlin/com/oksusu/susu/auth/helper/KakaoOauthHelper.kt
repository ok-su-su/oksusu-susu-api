package com.oksusu.susu.auth.helper

import com.oksusu.susu.auth.infrastructure.oauth.kakao.client.KakaoClient
import com.oksusu.susu.auth.model.dto.AbleRegisterResponse
import com.oksusu.susu.auth.model.dto.OauthLoginLinkResponse
import com.oksusu.susu.auth.model.dto.OauthTokenResponse
import com.oksusu.susu.auth.model.dto.OauthUserInfoDto
import com.oksusu.susu.common.properties.KakaoOauthProperties
import com.oksusu.susu.user.infrastructure.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

private const val KAKAO_OAUTH_QUERY_STRING = "/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code"
private const val KAKAO_KAUTH_URL = "https://kauth.kakao.com"

@Component
class KakaoOauthHelper(
    val kakaoOauthProperties: KakaoOauthProperties,
    val kakaoClient: KakaoClient,
    private val userRepository: UserRepository,
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
    suspend fun getOauthTokenDev(code: String): OauthTokenResponse =
        OauthTokenResponse.fromKakao(kakaoClient.kakaoTokenClient(kakaoOauthProperties.redirectUrl, code))

    /** 회원가입 가능 여부 체크. */
    suspend fun checkRegisterValid(accessToken: String): AbleRegisterResponse = withContext(Dispatchers.IO) {
        val userInfoDeferred = async {
            getKakaoUserInfo(accessToken)
        }
        val oauthInfo = userInfoDeferred.await().oauthInfo
        val canRegisterDeferred = async {
            userRepository.existsByOauthInfo(oauthInfo)
        }
        AbleRegisterResponse(!canRegisterDeferred.await())
    }

    /** 유저 정보를 가져옵니다. */
    suspend fun getKakaoUserInfo(accessToken: String): OauthUserInfoDto =
        OauthUserInfoDto.fromKakao(kakaoClient.kakaoUserInfoClient(accessToken))
}

package com.oksusu.susu.client.oauth.kakao

import com.oksusu.susu.auth.model.dto.OauthUserInfoDto
import com.oksusu.susu.auth.model.dto.response.OauthLoginLinkResponse
import com.oksusu.susu.auth.model.dto.response.OauthTokenResponse
import com.oksusu.susu.common.properties.KakaoOauthProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KakaoOauthService(
    val kakaoOauthProperties: KakaoOauthProperties,
    val kakaoClient: KakaoClient,
    @Value("\${server.domain-name}")
    private val domainName: String,
) {
    private val logger = mu.KotlinLogging.logger { }

    /** link */
    suspend fun getOauthLoginLinkDev(): OauthLoginLinkResponse {
        val redirectUrl = domainName + kakaoOauthProperties.redirectUrl
        return OauthLoginLinkResponse(
            kakaoOauthProperties.kauthUrl +
                String.format(
                    kakaoOauthProperties.authorizeUrl,
                    kakaoOauthProperties.clientId,
                    redirectUrl
                )
        )
    }

    suspend fun getOauthLoginLink(uri: String): OauthLoginLinkResponse {
        val redirectUrl = domainName + kakaoOauthProperties.withdrawCallbackUrl
        return OauthLoginLinkResponse(
            kakaoOauthProperties.kauthUrl +
                String.format(
                    kakaoOauthProperties.authorizeUrl,
                    kakaoOauthProperties.clientId,
                    redirectUrl
                )
        )
    }

    /** oauth token 받아오기 */
    suspend fun getOauthTokenDev(code: String): OauthTokenResponse {
        val redirectUrl = domainName + kakaoOauthProperties.redirectUrl
        return getKakaoToken(redirectUrl, code)
    }

    suspend fun getOauthToken(code: String, uri: String): OauthTokenResponse {
        val redirectUrl = domainName + kakaoOauthProperties.withdrawCallbackUrl
        return getKakaoToken(redirectUrl, code)
    }

    private suspend fun getKakaoToken(redirectUrl: String, code: String): OauthTokenResponse {
        return withContext(Dispatchers.IO) {
            kakaoClient.kakaoTokenClient(redirectUrl, code)
        }.run { OauthTokenResponse.fromKakao(this) }
    }

    /** 유저 정보를 가져옵니다. */
    suspend fun getKakaoUserInfo(accessToken: String): OauthUserInfoDto {
        return withContext(Dispatchers.IO) {
            kakaoClient.kakaoUserInfoClient(accessToken)
        }.run { OauthUserInfoDto.fromKakao(this) }
    }

    /** 회원 탈퇴합니다 */
    suspend fun withdraw(oauthId: String) {
        withContext(Dispatchers.IO) {
            kakaoClient.kakaoWithdrawClient(oauthId)
        }
    }
}

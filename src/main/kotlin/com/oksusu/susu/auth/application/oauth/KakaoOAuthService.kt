package com.oksusu.susu.auth.application.oauth

import com.oksusu.susu.auth.model.OAuthUserInfoDto
import com.oksusu.susu.auth.model.response.OAuthLoginLinkResponse
import com.oksusu.susu.auth.model.response.OAuthTokenResponse
import com.oksusu.susu.client.oauth.kakao.KakaoClient
import com.oksusu.susu.config.OAuthConfig
import com.oksusu.susu.extension.withMDCContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class KakaoOAuthService(
    val kakaoOAuthProperties: OAuthConfig.KakaoOAuthProperties,
    val kakaoClient: KakaoClient,
    @Value("\${server.domain-name}")
    private val domainName: String,
) {
    private val logger = KotlinLogging.logger { }

    /** link */
    suspend fun getOAuthLoginLinkDev(): OAuthLoginLinkResponse {
        val redirectUrl = domainName + kakaoOAuthProperties.redirectUrl
        return OAuthLoginLinkResponse(
            kakaoOAuthProperties.kauthUrl +
                String.format(
                    kakaoOAuthProperties.authorizeUrl,
                    kakaoOAuthProperties.clientId,
                    redirectUrl
                )
        )
    }

    suspend fun getOAuthLoginLink(uri: String): OAuthLoginLinkResponse {
        val redirectUrl = domainName + kakaoOAuthProperties.withdrawCallbackUrl
        return OAuthLoginLinkResponse(
            kakaoOAuthProperties.kauthUrl +
                String.format(
                    kakaoOAuthProperties.authorizeUrl,
                    kakaoOAuthProperties.clientId,
                    redirectUrl
                )
        )
    }

    /** oauth token 받아오기 */
    suspend fun getOAuthTokenDev(code: String): OAuthTokenResponse {
        val redirectUrl = domainName + kakaoOAuthProperties.redirectUrl
        return getKakaoToken(redirectUrl, code)
    }

    suspend fun getOAuthToken(code: String, uri: String): OAuthTokenResponse {
        val redirectUrl = domainName + kakaoOAuthProperties.withdrawCallbackUrl
        return getKakaoToken(redirectUrl, code)
    }

    private suspend fun getKakaoToken(redirectUrl: String, code: String): OAuthTokenResponse {
        return withContext(Dispatchers.IO.withMDCContext()) {
            kakaoClient.getToken(redirectUrl, code)
        }.run { OAuthTokenResponse.fromKakao(this) }
    }

    /** 유저 정보를 가져옵니다. */
    suspend fun getKakaoUserInfo(accessToken: String): OAuthUserInfoDto {
        return withContext(Dispatchers.IO.withMDCContext()) {
            kakaoClient.getUserInfo(accessToken)
        }.run { OAuthUserInfoDto.fromKakao(this) }
    }

    /** 회원 탈퇴합니다 */
    suspend fun withdraw(oAuthId: String) {
        withContext(Dispatchers.IO.withMDCContext()) {
            kakaoClient.withdraw(oAuthId)
        }
    }
}

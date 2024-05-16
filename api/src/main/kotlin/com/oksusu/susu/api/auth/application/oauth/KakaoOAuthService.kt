package com.oksusu.susu.api.auth.application.oauth

import com.oksusu.susu.api.auth.model.OAuthUserInfoDto
import com.oksusu.susu.api.auth.model.response.OAuthLoginLinkResponse
import com.oksusu.susu.api.auth.model.response.OAuthTokenResponse
import com.oksusu.susu.api.config.OAuthSecretConfig
import com.oksusu.susu.client.config.OAuthUrlConfig
import com.oksusu.susu.client.oauth.kakao.KakaoClient
import com.oksusu.susu.common.extension.withMDCContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class KakaoOAuthService(
    val kakaoOAuthSecretConfig: OAuthSecretConfig.KakaoOAuthSecretConfig,
    val kakaoOAuthUrlConfig: OAuthUrlConfig.KakaoOAuthUrlConfig,
    val kakaoClient: KakaoClient,
    @Value("\${server.domain-name}")
    private val domainName: String,
) {
    private val logger = KotlinLogging.logger { }

    /** link */
    suspend fun getOAuthLoginLinkDev(): OAuthLoginLinkResponse {
        val redirectUrl = domainName + kakaoOAuthUrlConfig.redirectUrl
        return OAuthLoginLinkResponse(
            kakaoOAuthUrlConfig.kauthUrl +
                String.format(
                    kakaoOAuthUrlConfig.authorizeUrl,
                    kakaoOAuthSecretConfig.clientId,
                    redirectUrl
                )
        )
    }

    suspend fun getOAuthWithdrawLoginLink(uri: String): OAuthLoginLinkResponse {
        val redirectUrl = domainName + kakaoOAuthUrlConfig.withdrawCallbackUrl
        return OAuthLoginLinkResponse(
            kakaoOAuthUrlConfig.kauthUrl +
                String.format(
                    kakaoOAuthUrlConfig.authorizeUrl,
                    kakaoOAuthSecretConfig.clientId,
                    redirectUrl
                )
        )
    }

    /** oauth token 받아오기 */
    suspend fun getOAuthTokenDev(code: String): OAuthTokenResponse {
        val redirectUrl = domainName + kakaoOAuthUrlConfig.redirectUrl
        return getKakaoToken(redirectUrl, code)
    }

    suspend fun getOAuthWithdrawToken(code: String): OAuthTokenResponse {
        val redirectUrl = domainName + kakaoOAuthUrlConfig.withdrawCallbackUrl
        return getKakaoToken(redirectUrl, code)
    }

    private suspend fun getKakaoToken(redirectUrl: String, code: String): OAuthTokenResponse {
        return withMDCContext(Dispatchers.IO) {
            kakaoClient.getToken(
                redirectUrl = redirectUrl,
                code = code,
                clientId = kakaoOAuthSecretConfig.clientId,
                clientSecret = kakaoOAuthSecretConfig.clientSecret
            )
        }.run { OAuthTokenResponse.fromKakao(this) }
    }

    /** 유저 정보를 가져옵니다. */
    suspend fun getOAuthInfo(accessToken: String): OAuthUserInfoDto {
        return withMDCContext(Dispatchers.IO) {
            kakaoClient.getUserInfo(accessToken)
        }.run { OAuthUserInfoDto.fromKakao(this) }
    }

    /** 회원 탈퇴합니다 */
    suspend fun withdraw(oAuthId: String) {
        withMDCContext(Dispatchers.IO) {
            kakaoClient.withdraw(targetId = oAuthId, adminKey = kakaoOAuthSecretConfig.adminKey)
        }
    }
}

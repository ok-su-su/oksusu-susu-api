package com.oksusu.susu.api.auth.application.oauth

import com.oksusu.susu.api.auth.model.OAuthUserInfoDto
import com.oksusu.susu.api.auth.model.response.OAuthLoginLinkResponse
import com.oksusu.susu.api.auth.model.response.OAuthTokenResponse
import com.oksusu.susu.api.config.OAuthSecretConfig
import com.oksusu.susu.client.config.OAuthUrlConfig
import com.oksusu.susu.client.oauth.google.GoogleClient
import com.oksusu.susu.client.oauth.kakao.KakaoClient
import com.oksusu.susu.common.extension.withMDCContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GoogleOAuthService(
    val kakaoOAuthSecretConfig: OAuthSecretConfig.KakaoOAuthSecretConfig,
    val kakaoOAuthUrlConfig: OAuthUrlConfig.KakaoOAuthUrlConfig,
    val googleOAuthUrlConfig: OAuthUrlConfig.GoogleOAuthUrlConfig,
    val googleOAuthSecretConfig: OAuthSecretConfig.GoogleOAuthSecretConfig,
    val kakaoClient: KakaoClient,
    val googleClient: GoogleClient,
    @Value("\${server.domain-name}")
    private val domainName: String,
) {
    private val logger = KotlinLogging.logger { }

    // https://accounts.google.com/o/oauth2/v2/auth?response_type=code&redirect_uri=http://localhost:8080/api/v1/dev/oauth/GOOGLE/token&client_id=160906391437-e7pt62v3qj9rdpr20k7qpab6p3gj64m5.apps.googleusercontent.com&scope=https://www.googleapis.com/auth/userinfo.profile
    /** link */
    suspend fun getOAuthLoginLinkDev(): OAuthLoginLinkResponse {
        val redirectUrl = domainName + googleOAuthUrlConfig.redirectUrl
        return OAuthLoginLinkResponse(
            googleOAuthUrlConfig.accountGoogleUrl +
                String.format(
                    googleOAuthUrlConfig.authorizeUrl,
                    googleOAuthSecretConfig.clientId,
                    redirectUrl
                )
        )
    }

    suspend fun getOAuthWithdrawLoginLink(uri: String): OAuthLoginLinkResponse {
        val redirectUrl = domainName + googleOAuthUrlConfig.withdrawCallbackUrl
        return OAuthLoginLinkResponse(
            googleOAuthUrlConfig.accountGoogleUrl +
                    String.format(
                        googleOAuthUrlConfig.authorizeUrl,
                        googleOAuthSecretConfig.clientId,
                        redirectUrl
                    )
        )
    }

    /** oauth token 받아오기 */
    suspend fun getOAuthTokenDev(code: String): OAuthTokenResponse {
        val redirectUrl = domainName + googleOAuthUrlConfig.redirectUrl
        return getGoogleToken(redirectUrl, code)
    }

    suspend fun getOAuthWithdrawToken(code: String): OAuthTokenResponse {
        val redirectUrl = domainName + kakaoOAuthUrlConfig.withdrawCallbackUrl
        return getGoogleToken(redirectUrl, code)
    }

    private suspend fun getGoogleToken(redirectUrl: String, code: String): OAuthTokenResponse {
        logger.info { code }
        return withMDCContext(Dispatchers.IO) {
            googleClient.getToken(
                redirectUrl = redirectUrl,
                code = code,
                clientId = googleOAuthSecretConfig.clientId,
                clientSecret = googleOAuthSecretConfig.clientSecret
            )
        }.run { OAuthTokenResponse.fromGoogle(this) }
    }

    /** 유저 정보를 가져옵니다. */
    suspend fun getOAuthInfo(accessToken: String): OAuthUserInfoDto {
        return withMDCContext(Dispatchers.IO) {
            googleClient.getUserInfo(accessToken)
        }.run { OAuthUserInfoDto.fromGoogle(this) }
    }

    /** 회원 탈퇴합니다 */
    suspend fun withdraw(oAuthId: String) {
        withMDCContext(Dispatchers.IO) {
            kakaoClient.withdraw(targetId = oAuthId, adminKey = kakaoOAuthSecretConfig.adminKey)
        }
    }
}

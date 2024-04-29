package com.oksusu.susu.api.auth.application.oauth

import com.oksusu.susu.api.auth.model.OAuthUserInfoDto
import com.oksusu.susu.api.auth.model.OidcDecodePayload
import com.oksusu.susu.api.auth.model.response.OAuthLoginLinkResponse
import com.oksusu.susu.api.auth.model.response.OAuthTokenResponse
import com.oksusu.susu.api.config.OAuthSecretConfig
import com.oksusu.susu.client.config.OAuthUrlConfig
import com.oksusu.susu.client.oauth.google.GoogleClient
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import com.oksusu.susu.domain.user.domain.vo.OauthInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GoogleOAuthService(
    val googleOAuthUrlConfig: OAuthUrlConfig.GoogleOAuthUrlConfig,
    val googleOAuthSecretConfig: OAuthSecretConfig.GoogleOAuthSecretConfig,
    val googleClient: GoogleClient,
    val oidcService: OidcService,
    @Value("\${server.domain-name}")
    private val domainName: String,
) {
    private val logger = KotlinLogging.logger { }

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
        val redirectUrl = domainName + googleOAuthUrlConfig.withdrawCallbackUrl
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
        }.run {
            logger.info { this.idToken }
            OAuthTokenResponse.fromGoogle(this)
        }
    }

    /** 유저 정보를 가져옵니다. */
    suspend fun getOAuthInfo(accessToken: String): OAuthUserInfoDto {
        return withMDCContext(Dispatchers.IO) {
            googleClient.getUserInfo(accessToken)
        }.run { OAuthUserInfoDto.fromGoogle(this) }
    }


    /** 유저 정보를 가져옵니다. */
    suspend fun getOAuthInfoWithOidc(idToken: String): OAuthUserInfoDto {
        return withMDCContext(Dispatchers.IO) {
            getOIDCDecodePayload(idToken)
        }.run { OAuthUserInfoDto(
            oauthInfo = OauthInfo(
                oAuthProvider = OAuthProvider.GOOGLE,
                oAuthId = this.sub
            )
        ) }
    }

    /** 회원 탈퇴합니다 */
    suspend fun withdraw(accessToken: String) {
        withMDCContext(Dispatchers.IO) {
            googleClient.withdraw(accessToken = accessToken)
        }
    }

    /**
     * oidc decode
     */
    private suspend fun getOIDCDecodePayload(token: String): OidcDecodePayload {
        val oidcPublicKeysResponse = oidcService.getOidcPublicKeys(OAuthProvider.GOOGLE)
        return oidcService.getPayloadFromIdToken(
            token,
            googleOAuthUrlConfig.accountGoogleUrl,
            googleOAuthSecretConfig.clientId,
            oidcPublicKeysResponse
        )
    }
}

package com.oksusu.susu.api.auth.application.oauth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.ECDSAKeyProvider
import com.oksusu.susu.api.auth.model.OAuthUserInfoDto
import com.oksusu.susu.api.auth.model.OidcDecodePayload
import com.oksusu.susu.api.auth.model.response.OAuthLoginLinkResponse
import com.oksusu.susu.api.auth.model.response.OAuthTokenResponse
import com.oksusu.susu.api.config.OAuthSecretConfig
import com.oksusu.susu.client.config.OAuthUrlConfig
import com.oksusu.susu.client.oauth.apple.AppleClient
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.bouncycastle.util.io.pem.PemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.*
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date

@Service
class AppleOAuthService(
    private val appleOAuthSecretConfig: OAuthSecretConfig.AppleOAuthSecretConfig,
    private val appleOAuthUrlConfig: OAuthUrlConfig.AppleOAuthUrlConfig,
    private val appleClient: AppleClient,
    @Value("\${server.domain-name}")
    private val domainName: String,
    private val oidcService: OidcService,
) {
    private val logger = KotlinLogging.logger { }

    /** link */
    suspend fun getOAuthLoginLinkDev(): OAuthLoginLinkResponse {
        val redirectUrl = domainName + appleOAuthUrlConfig.devCallbackUrl
        return OAuthLoginLinkResponse(
            appleOAuthUrlConfig.appleIdUrl +
                String.format(
                    appleOAuthUrlConfig.authorizeUrl,
                    appleOAuthSecretConfig.clientId,
                    redirectUrl
                )
        )
    }

    suspend fun getOAuthWithdrawLoginLink(uri: String): OAuthLoginLinkResponse {
        val redirectUrl = domainName + appleOAuthUrlConfig.withdrawCallbackUrl
        return OAuthLoginLinkResponse(
            appleOAuthUrlConfig.appleIdUrl +
                String.format(
                    appleOAuthUrlConfig.authorizeUrl,
                    appleOAuthSecretConfig.clientId,
                    redirectUrl
                )
        )
    }

    /** oauth token 받아오기 */
    suspend fun getOAuthTokenDev(code: String): OAuthTokenResponse {
        val redirectUrl = domainName + appleOAuthUrlConfig.devCallbackUrl
        return getAppleToken(redirectUrl, code, appleOAuthSecretConfig.clientId, getClientSecretDev())
    }

    suspend fun getOAuthWithdrawToken(code: String): OAuthTokenResponse {
        val redirectUrl = domainName + appleOAuthUrlConfig.withdrawCallbackUrl
        return getAppleToken(redirectUrl, code, appleOAuthSecretConfig.clientId, getClientSecret())
    }

    private suspend fun getAppleToken(
        redirectUrl: String,
        code: String,
        clientId: String,
        clientSecret: String,
    ): OAuthTokenResponse {
        val tokens = withMDCContext(Dispatchers.IO) {
            appleClient.getToken(redirectUrl, code, clientId, clientSecret)
        }

        getOIDCDecodePayload(tokens.idToken)

        return OAuthTokenResponse.fromApple(tokens)
    }

    /**
     * idtoken 분석
     */
    suspend fun getOAuthInfo(idToken: String): OAuthUserInfoDto {
        val oidcDecodePayload = getOIDCDecodePayload(idToken)
        return OAuthUserInfoDto.fromApple(oidcDecodePayload.sub)
    }

    /**
     * oidc decode
     */
    private suspend fun getOIDCDecodePayload(token: String): OidcDecodePayload {
        val oidcPublicKeysResponse = oidcService.getOidcPublicKeys(OAuthProvider.APPLE)
        return oidcService.getPayloadFromIdToken(
            token,
            appleOAuthUrlConfig.appleIdUrl,
            appleOAuthSecretConfig.clientId,
            oidcPublicKeysResponse
        )
    }

    /** 회원 탈퇴합니다 */
    suspend fun withdraw(code: String) {
        val tokens = getOAuthWithdrawToken(code)

        withMDCContext(Dispatchers.IO) {
            appleClient.withdraw(
                appleOAuthSecretConfig.clientId,
                tokens.accessToken,
                getClientSecret()
            )
        }
    }

    /**
     * client secret 가져오기
     */
    private fun getClientSecret(): String {
        return createClientSecret(appleOAuthSecretConfig.clientId)
    }

    private fun getClientSecretDev(): String {
        return createClientSecret(appleOAuthSecretConfig.clientId)
    }

    private fun createClientSecret(clientId: String): String {
        val iat = Date().toInstant()
        val exp = Date(iat.epochSecond + 3600000).toInstant()

        val headers = mapOf("alg" to "ES256", "kid" to appleOAuthSecretConfig.keyId)

        val unsignedJWT = JWT.create().apply {
            this.withHeader(headers)
            this.withIssuer(appleOAuthSecretConfig.teamId)
            this.withAudience(appleOAuthUrlConfig.appleIdUrl)
            this.withIssuedAt(iat)
            this.withExpiresAt(exp)
            this.withSubject(clientId)
        }

        return try {
            unsignedJWT.sign(Algorithm.ECDSA256(KeyProvider(appleOAuthSecretConfig)))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException(e)
        }
    }
}

class KeyProvider(
    private val appleOAuthSecretConfig: OAuthSecretConfig.AppleOAuthSecretConfig,
) : ECDSAKeyProvider {

    override fun getPublicKeyById(keyId: String?): ECPublicKey? = null

    override fun getPrivateKey(): ECPrivateKey {
        val authKey = appleOAuthSecretConfig.authKey
        val byteAuthKey = authKey.replace("((()))", "\n").toByteArray()

        val keyInputStream = ByteArrayInputStream(byteAuthKey)
        val keyReader = InputStreamReader(keyInputStream)

        val content = PemReader(keyReader).readPemObject().content

        val kf = KeyFactory.getInstance("EC")
        val spec = PKCS8EncodedKeySpec(content)

        return kf.generatePrivate(spec) as ECPrivateKey
    }

    override fun getPrivateKeyId(): String? = null
}

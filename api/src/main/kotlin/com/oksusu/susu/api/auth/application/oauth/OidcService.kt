package com.oksusu.susu.api.auth.application.oauth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.oksusu.susu.api.auth.model.OidcDecodePayload
import com.oksusu.susu.api.event.model.CacheAppleOidcPublicKeysEvent
import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.model.OidcPublicKeysCacheModel
import com.oksusu.susu.cache.model.vo.OidcPublicKeyCacheModel
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.client.oauth.apple.AppleClient
import com.oksusu.susu.client.oauth.oidc.model.OidcPublicKeyModel
import com.oksusu.susu.client.oauth.oidc.model.OidcPublicKeysResponse
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidTokenException
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import java.util.Date

@Component
class OidcService(
    private val cacheService: CacheService,
    private val appleClient: AppleClient,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun getOidcPublicKeys(provider: OAuthProvider): OidcPublicKeysResponse {
        withMDCContext(Dispatchers.IO) {
            when (provider) {
                OAuthProvider.APPLE -> cacheService.getOrNull(Cache.getAppleOidcPublicKeyCache())
                else -> null
            }
        }?.run {
            logger.debug { "apple oidc public key cache hit" }

            val models = this.keys.map { key ->
                OidcPublicKeyModel(
                    kid = key.kid,
                    alg = key.alg,
                    use = key.use,
                    n = key.n,
                    e = key.e
                )
            }

            return OidcPublicKeysResponse(models)
        }

        return withMDCContext(Dispatchers.IO) {
            appleClient.getOidcPublicKeys()
        }.run {
            val cachekeys = this.keys.map { key ->
                OidcPublicKeyCacheModel(
                    kid = key.kid,
                    alg = key.alg,
                    use = key.use,
                    n = key.n,
                    e = key.e
                )
            }

            /** oidc pk 캐싱 */
            eventPublisher.publishEvent(
                CacheAppleOidcPublicKeysEvent(
                    keys = OidcPublicKeysCacheModel(cachekeys)
                )
            )

            this
        }
    }

    fun getPayloadFromIdToken(
        token: String,
        iss: String,
        aud: String,
        oidcPublicKeysResponse: OidcPublicKeysResponse,
    ): OidcDecodePayload {
        val jwt = decodeIdToken(token, oidcPublicKeysResponse.keys)
        logger.info { "1" }
        jwt ?: throw InvalidTokenException(ErrorCode.INVALID_TOKEN)

        verifyToken(jwt, iss, aud)

        return OidcDecodePayload(
            iss = jwt.getClaim("iss").asString(),
            aud = jwt.getClaim("aud").asString(),
            sub = jwt.getClaim("sub").asString()
        )
    }

    private fun decodeIdToken(token: String, keys: List<OidcPublicKeyModel>): DecodedJWT? {
        keys.forEach {
            val nBytes: ByteArray = Base64.getUrlDecoder().decode(it.n)
            val eBytes: ByteArray = Base64.getUrlDecoder().decode(it.e)
            val modules = BigInteger(1, nBytes)
            val exponent = BigInteger(1, eBytes)

            val spec = RSAPublicKeySpec(modules, exponent)
            val kf = KeyFactory.getInstance("RSA")
            val publicKey = kf.generatePublic(spec) as RSAPublicKey

            try {
                return JWT.require(
                    Algorithm.RSA256(
                        publicKey,
                        null
                    )
                ).build().verify(token)
            } catch (e: Exception) {
            }
        }
        return null
    }

    private fun verifyToken(token: DecodedJWT, iss: String, aud: String) {
        val verifyTime = Date() < token.expiresAt
        val verifyAud = token.audience.firstOrNull() == aud
        val verifyIssuer = token.issuer == iss

        logger.info { "2" }
        if (!verifyTime || !verifyAud || !verifyIssuer) {
            throw InvalidTokenException(ErrorCode.INVALID_TOKEN)
        }
    }
}

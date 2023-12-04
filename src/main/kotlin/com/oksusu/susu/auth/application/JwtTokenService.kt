package com.oksusu.susu.auth.application

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.readValue
import com.oksusu.susu.auth.model.AuthUserToken
import com.oksusu.susu.auth.model.AuthUserTokenPayload
import com.oksusu.susu.config.jwt.JwtConfig
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidTokenException
import com.oksusu.susu.extension.decodeBase64
import com.oksusu.susu.extension.mapper
import com.oksusu.susu.extension.toInstant
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Service
class JwtTokenService(
    private val jwtConfig: JwtConfig,
) {
    private val logger = mu.KotlinLogging.logger {}

    private val jwtVerifier = JWT
        .require(Algorithm.HMAC256(jwtConfig.secretKey))
        .withIssuer(jwtConfig.issuer)
        .withAudience(jwtConfig.audience)
        .build()

    fun createToken(id: Long, tokenExpiredAt: LocalDateTime): String {
        return JWT.create().apply {
            this.withIssuer(jwtConfig.issuer)
            this.withAudience(jwtConfig.audience)
            this.withClaim("id", id)
            this.withExpiresAt(Date.from(tokenExpiredAt.toInstant()))
        }.sign(Algorithm.HMAC256(jwtConfig.secretKey))
    }

    fun verifyToken(token: AuthUserToken): AuthUserTokenPayload {
        val payload = jwtVerifier.verify(token.value)
            .payload
            .decodeBase64()

        return mapper.readValue(payload)
    }

    fun verifyTokenMono(authUserToken: Mono<AuthUserToken>): Mono<AuthUserTokenPayload> {
        return authUserToken.flatMap { jwtToken ->
            Mono.fromCallable { verifyToken(jwtToken) }
                .onErrorResume { e ->
                    logger.warn { e.message }
                    Mono.error(InvalidTokenException(ErrorCode.FAIL_TO_VERIFY_TOKEN_ERROR))
                }
        }
    }
}

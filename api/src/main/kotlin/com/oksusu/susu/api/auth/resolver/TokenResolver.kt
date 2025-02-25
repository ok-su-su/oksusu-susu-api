package com.oksusu.susu.api.auth.resolver

import com.oksusu.susu.api.auth.model.AUTH_TOKEN_KEY
import com.oksusu.susu.api.auth.model.AuthUserToken
import org.springframework.http.server.reactive.ServerHttpRequest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

data class TokenResolver(
    private val request: ServerHttpRequest,
) {
    fun resolveToken(): Mono<AuthUserToken> {
        val authUserToken = request.headers
            .asSequence()
            .filter { header -> isTokenHeader(header.key) }
            .mapNotNull { header ->
                header.value
                    .firstOrNull()
                    ?.takeIf { token -> token.isNotBlank() }
                    ?.let { token -> AuthUserToken.from(token) }
            }.firstOrNull() ?: AuthUserToken.from("")

        return authUserToken.toMono()
    }

    private fun isTokenHeader(headerKey: String): Boolean {
        return AUTH_TOKEN_KEY.equals(headerKey, ignoreCase = true)
    }
}

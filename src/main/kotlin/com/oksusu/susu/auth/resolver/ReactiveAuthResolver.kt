package com.oksusu.susu.auth.resolver

import com.oksusu.susu.auth.application.AuthService
import com.oksusu.susu.auth.model.AUTH_TOKEN_KEY
import com.oksusu.susu.auth.model.AuthUserToken
import com.oksusu.susu.auth.model.AuthUser
import org.springframework.core.MethodParameter
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolverSupport
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class ReactiveAuthResolver(
    adapterRegistry: ReactiveAdapterRegistry,
    private val authService: AuthService,
) : HandlerMethodArgumentResolverSupport(adapterRegistry) {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == AuthUser::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange,
    ): Mono<Any> {
        val tokenMono = resolveToken(exchange.request)
        return authService.resolveAuthUser(tokenMono)
    }

    private fun resolveToken(request: ServerHttpRequest): Mono<AuthUserToken> {
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

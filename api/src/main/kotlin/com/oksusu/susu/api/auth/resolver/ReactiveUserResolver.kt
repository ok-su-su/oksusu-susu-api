package com.oksusu.susu.api.auth.resolver

import com.oksusu.susu.api.auth.application.AuthFacade
import com.oksusu.susu.api.auth.model.AdminUser
import com.oksusu.susu.api.auth.model.AuthUser
import org.springframework.core.MethodParameter
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolverSupport
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class ReactiveUserResolver(
    adapterRegistry: ReactiveAdapterRegistry,
    private val authFacade: AuthFacade,
) : HandlerMethodArgumentResolverSupport(adapterRegistry) {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == AuthUser::class.java || parameter.parameterType == AdminUser::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange,
    ): Mono<Any> {
        val tokenMono = TokenResolver(exchange.request).resolveToken()

        return if (parameter.parameterType == AuthUser::class.java) {
            authFacade.resolveAuthUser(tokenMono)
        } else {
            authFacade.resolveAdminUser(tokenMono)
        }
    }
}

package com.oksusu.susu.api.user.resolver

import com.oksusu.susu.api.user.model.DEVICE_INFO_HEADERS
import com.oksusu.susu.api.user.model.UserDeviceContext
import com.oksusu.susu.api.user.model.UserDeviceContextImpl
import org.springframework.core.MethodParameter
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolverSupport
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class ReactiveDeviceContextResolver(
    adapterRegistry: ReactiveAdapterRegistry,
) : HandlerMethodArgumentResolverSupport(adapterRegistry) {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == UserDeviceContext::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange,
    ): Mono<Any> {
        val headers = parseHeaders(exchange.request)
        return UserDeviceContextImpl(
            applicationVersion = headers["Application-Version"],
            deviceId = headers["Device-Id"],
            deviceSoftwareVersion = headers["Device-Software-Version"],
            lineNumber = headers["Line-Number"],
            networkCountryIso = headers["Network-Country-Iso"],
            networkOperator = headers["Network-Operator"],
            networkOperatorName = headers["Network-Operator-Name"],
            networkType = headers["Network-Type"],
            phoneType = headers["Phone-Type"],
            simSerialNumber = headers["Sim-Serial-Number"],
            simState = headers["Sim-State"]
        ).toMono()
    }

    private fun parseHeaders(request: ServerHttpRequest): Map<String, String> {
        return request.headers
            .asSequence()
            .filter { header -> isDeviceContextHeader(header.key) }
            .associate { header -> header.key to header.key }
    }

    private fun isDeviceContextHeader(headerKey: String): Boolean {
        return DEVICE_INFO_HEADERS.contains(headerKey)
    }
}

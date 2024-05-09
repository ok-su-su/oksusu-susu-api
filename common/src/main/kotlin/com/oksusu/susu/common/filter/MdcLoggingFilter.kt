package com.oksusu.susu.common.filter

import com.oksusu.susu.common.extension.createPutWithMDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcLoggingFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange)
            .contextWrite { it.createPutWithMDC("requestId", exchange.request.id) }
    }
}

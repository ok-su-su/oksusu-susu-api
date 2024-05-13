package com.oksusu.susu.common.filter

import com.oksusu.susu.common.consts.TRACE_ID
import org.jboss.logging.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * MDC 값을 Reactor Context에 넣어주는 필터
 * filter 중 가장 먼저 수행됨
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcLoggingFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange)
            .contextWrite { it.put(TRACE_ID, MDC.get(TRACE_ID))}
    }
}

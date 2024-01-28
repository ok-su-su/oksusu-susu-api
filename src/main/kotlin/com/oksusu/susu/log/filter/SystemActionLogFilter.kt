package com.oksusu.susu.log.filter

import com.oksusu.susu.event.model.SystemActionLogEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class SystemActionLogFilter(
    private val publisher: ApplicationEventPublisher,
) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        publisher.publishEvent(SystemActionLogEvent.from(exchange))
        return chain.filter(exchange)
    }
}

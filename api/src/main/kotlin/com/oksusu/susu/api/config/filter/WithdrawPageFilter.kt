package com.oksusu.susu.api.config.filter

import com.oksusu.susu.api.event.model.SystemActionLogEvent
import com.oksusu.susu.common.exception.NotFoundException
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class WithdrawPageFilter : WebFilter {
    companion object {
        val withdrawPagePath = listOf(
            "/withdraw/login",
            "/withdraw",
            "/kakao/callback",

            "/css/withdrawLogin.css",
            "/css/withdraw.css",

            "/apple-touch-icon.png",
            "/susufavicon_16_16.png",
            "/susufavicon_32_32.png",
            "/susufavicon_192_192.png",
            "/kakaoLogin.png",
        )
        val withdrawPageHost = listOf(
            "oksusu.site",
            "localhost:8080",
        )
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request

        val host = request.headers["Host"].toString().removePrefix("[").removeSuffix("]")
        val path = request.uri.path

        if (path in withdrawPagePath && host !in withdrawPageHost){
            throw NoSuchElementException()
        }

        if (host in withdrawPageHost && path !in withdrawPagePath){
            throw NoSuchElementException()
        }

        return chain.filter(exchange)
    }
}

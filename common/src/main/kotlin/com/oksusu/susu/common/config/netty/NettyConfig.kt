package com.oksusu.susu.common.config.netty

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.server.HttpServer

@Configuration
class NettyConfig {
    @Bean
    fun nettyReactiveWebServerFactory(): NettyReactiveWebServerFactory{
        val webServerFactory = NettyReactiveWebServerFactory()
        webServerFactory.addServerCustomizers(HttpServerCustomizer())
        return webServerFactory
    }
}

/**
 * handler 추가하는 netty server customizer
 */
class HttpServerCustomizer : NettyServerCustomizer {
    override fun apply(httpServer: HttpServer): HttpServer {
        return httpServer.doOnConnection { conn ->
            conn.addHandlerLast(MdcChannelInboundHandler())
        }
    }
}

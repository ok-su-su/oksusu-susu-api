package com.oksusu.susu.api.config.filter

import io.netty.util.internal.EmptyArrays.EMPTY_BYTES
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class RequestCachingFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return DataBufferUtils
            .join(exchange.request.body)
            .map { databuffer ->
                // request.body byte[] 형태로 가져오기
                val bytes = ByteArray(databuffer.readableByteCount())
                DataBufferUtils.release(databuffer.read(bytes))

                bytes
            }
            .defaultIfEmpty(EMPTY_BYTES)
            .flatMap { bytes ->
                // request body caching
                val decorator = RequestBodyDecorator(exchange, bytes)

                // return serverWebExchange
                chain.filter(exchange.mutate().request(decorator).build())
            }
    }
}

class RequestBodyDecorator(
    val exchange: ServerWebExchange,
    val bytes: ByteArray,
) : ServerHttpRequestDecorator(exchange.request) {
    override fun getBody(): Flux<DataBuffer> {
        return if (bytes == null || bytes.size == 0) {
            Flux.empty()
        } else {
            Flux.just(
                exchange.response.bufferFactory().wrap(bytes)
            )
        }
    }
}

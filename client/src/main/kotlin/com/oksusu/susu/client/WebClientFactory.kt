package com.oksusu.susu.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.*
import reactor.core.publisher.Mono
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import java.util.concurrent.TimeUnit

class WebClientFactory {
    companion object {
        fun generate(
            baseUrl: String,
            connectionTimeoutMillis: Int = 1000,
            readTimeoutMillis: Long = 1000,
            writeTimeoutMillis: Long = 1000,
        ): WebClient {
            val clientHttpConnector = createFactory(
                connectionTimeoutMillis = connectionTimeoutMillis,
                readTimeoutMillis = readTimeoutMillis,
                writeTimeoutMillis = writeTimeoutMillis
            )

            return WebClient.builder()
                .baseUrl(baseUrl)
                .codecs { it.defaultCodecs().enableLoggingRequestDetails(true) }
                .clientConnector(clientHttpConnector)
                .build()
        }

        fun generateWithoutBaseUrl(
            connectionTimeoutMillis: Int = 1000,
            readTimeoutMillis: Long = 1000,
            writeTimeoutMillis: Long = 1000,
        ): WebClient {
            val clientHttpConnector = createFactory(
                connectionTimeoutMillis = connectionTimeoutMillis,
                readTimeoutMillis = readTimeoutMillis,
                writeTimeoutMillis = writeTimeoutMillis
            )

            return WebClient.builder()
                .codecs { it.defaultCodecs().enableLoggingRequestDetails(true) }
                .clientConnector(clientHttpConnector)
//                .filter(ResponseLoggingFilter())
                .build()
        }

        private fun createFactory(
            connectionTimeoutMillis: Int,
            readTimeoutMillis: Long,
            writeTimeoutMillis: Long,
        ): ClientHttpConnector {
            val httpClient = httpClient(
                connectionTimeoutMillis = connectionTimeoutMillis,
                readTimeoutMillis = readTimeoutMillis,
                writeTimeoutMillis = writeTimeoutMillis
            )

            return ReactorClientHttpConnector(httpClient)
        }

        private fun httpClient(
            connectionTimeoutMillis: Int,
            readTimeoutMillis: Long,
            writeTimeoutMillis: Long,
        ): HttpClient {
            return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMillis)
                .doOnConnected { connection: Connection ->
                    connection
                        .addHandlerLast(ReadTimeoutHandler(readTimeoutMillis, TimeUnit.MILLISECONDS))
                        .addHandlerLast(WriteTimeoutHandler(writeTimeoutMillis, TimeUnit.MILLISECONDS))
                }.apply {
                    this.warmup().block()
                    this.compress(true)
                }
        }
    }
}

/**
 * webclient 결과값 디버깅용
 * response body 로깅 필터
 */
class ResponseLoggingFilter : ExchangeFilterFunction {
    val logger = KotlinLogging.logger { }

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        return next.exchange(request)
            .flatMap { response ->
                response.bodyToMono(String::class.java).flatMap { body ->
                    logger.info("[${response.statusCode()}] $body")
                    Mono.just(response)
                }
            }
    }
}

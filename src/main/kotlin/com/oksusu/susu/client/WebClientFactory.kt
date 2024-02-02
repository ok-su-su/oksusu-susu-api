package com.oksusu.susu.client

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
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

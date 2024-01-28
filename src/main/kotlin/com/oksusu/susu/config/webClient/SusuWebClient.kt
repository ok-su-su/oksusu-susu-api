package com.oksusu.susu.config.webClient

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.http.codec.LoggingCodecSupport
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class SusuWebClient {
    private val logger = KotlinLogging.logger { }

    @Bean
    fun webClient(): WebClient {
        val exchangeStrategies = ExchangeStrategies.builder().build()
        exchangeStrategies.messageWriters().stream() // logging 관련 설정
            .filter { obj: HttpMessageWriter<*> ->
                LoggingCodecSupport::class.java.isInstance(
                    obj
                )
            }
            .forEach { writer: HttpMessageWriter<*> ->
                (writer as LoggingCodecSupport).isEnableLoggingRequestDetails = true
            }

        val httpClient: HttpClient = HttpClient.create() // timeout
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofMillis(5000))
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                    .addHandlerLast(WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))
            }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(exchangeStrategies)
            .filter(
                ExchangeFilterFunction.ofRequestProcessor { clientRequest: ClientRequest -> // request logging
//                    logger.info(">>>>>>>>> REQUEST <<<<<<<<<<")
//                    logger.info("Request: {} {}", clientRequest.method(), clientRequest.url())
//                    clientRequest.headers().forEach { name: String?, values: List<String?> ->
//                        values.forEach(
//                            Consumer<String?> { value: String? ->
//                                logger.info(
//                                    "{} : {}",
//                                    name,
//                                    value
//                                )
//                            }
//                        )
//                    }
                    Mono.just(clientRequest)
                }
            ) // Response Header 로깅 필터
            .filter(
                ExchangeFilterFunction.ofResponseProcessor { clientResponse: ClientResponse -> // response logging
//                    logger.info(">>>>>>>>>> RESPONSE <<<<<<<<<<")
//                    clientResponse.headers().asHttpHeaders().forEach { name: String?, values: List<String?> ->
//                        values.forEach(
//                            Consumer<String?> { value: String? ->
//                                logger.info(
//                                    "{} {}",
//                                    name,
//                                    value
//                                )
//                            })
//                    }
                    Mono.just(clientResponse)
                }
            )
            .defaultHeader("Content-type", "application/x-www-form-urlencoded")
            .build()
    }
}

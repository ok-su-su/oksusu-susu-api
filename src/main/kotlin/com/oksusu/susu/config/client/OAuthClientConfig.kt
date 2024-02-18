package com.oksusu.susu.config.client

import com.oksusu.susu.client.WebClientFactory
import com.oksusu.susu.client.oauth.kakao.KakaoClient
import com.oksusu.susu.client.oauth.kakao.SuspendableKakaoClient
import com.oksusu.susu.config.OAuthConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OAuthClientConfig (
    private val kakaoOAuthProperties: OAuthConfig.KakaoOAuthProperties,
){
    private val logger = KotlinLogging.logger {}

    @Bean
    fun kakaoClient(): KakaoClient {
        val webClient = WebClientFactory.generateWithoutBaseUrl()
        logger.info { "initialized oauth client" }
        return SuspendableKakaoClient(webClient, kakaoOAuthProperties)
    }
}
package com.oksusu.susu.client.config

import com.oksusu.susu.client.WebClientFactory
import com.oksusu.susu.client.oauth.apple.AppleClient
import com.oksusu.susu.client.oauth.apple.SuspendableAppleClient
import com.oksusu.susu.client.oauth.google.GoogleClient
import com.oksusu.susu.client.oauth.google.SuspendableGoogleClient
import com.oksusu.susu.client.oauth.kakao.KakaoClient
import com.oksusu.susu.client.oauth.kakao.SuspendableKakaoClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OAuthClientConfig(
    private val kakaoOAuthUrlConfig: OAuthUrlConfig.KakaoOAuthUrlConfig,
    private val appleOAuthUrlConfig: OAuthUrlConfig.AppleOAuthUrlConfig,
    private val googleOAuthUrlConfig: OAuthUrlConfig.GoogleOAuthUrlConfig,
) {
    private val logger = KotlinLogging.logger {}

    @Bean
    fun kakaoClient(): KakaoClient {
        val webClient = WebClientFactory.generateWithoutBaseUrl()
        logger.info { "initialized oauth kakao client" }
        return SuspendableKakaoClient(webClient, kakaoOAuthUrlConfig)
    }

    @Bean
    fun appleClient(): AppleClient {
        val webClient = WebClientFactory.generateWithoutBaseUrl()
        logger.info { "initialized oauth apple client" }
        return SuspendableAppleClient(webClient, appleOAuthUrlConfig)
    }

    @Bean
    fun googleClient(): GoogleClient {
        val webClient = WebClientFactory.generateWithoutBaseUrl()
        logger.info { "initialized oauth google client" }
        return SuspendableGoogleClient(webClient, googleOAuthUrlConfig)
    }
}

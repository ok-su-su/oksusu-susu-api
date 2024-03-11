package com.oksusu.susu.api.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    OAuthConfig.KakaoOAuthProperties::class
)
class OAuthConfig(
    val kakaoOAuthProperties: KakaoOAuthProperties,
) {
    init {
        val logger = KotlinLogging.logger { }
        OAuthConfig::class.declaredMemberProperties
            .forEach { config ->
                logger.info { config.get(this).toString() }
            }
    }

    @ConfigurationProperties(prefix = "oauth.kakao")
    class KakaoOAuthProperties(
        val withdrawCallbackUrl: String,
        val unlinkUrl: String,
        val userInfoUrl: String,
        val authorizeUrl: String,
        val tokenUrl: String,
        val kauthUrl: String,
        val kapiUrl: String,
        val clientId: String,
        val clientSecret: String,
        val redirectUrl: String,
        val appKey: String,
        val adminKey: String,
    )
}

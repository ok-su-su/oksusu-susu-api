package com.oksusu.susu.api.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    OAuthSecretConfig.KakaoOAuthSecretConfig::class,
    OAuthSecretConfig.AppleOAuthSecretConfig::class,
    OAuthSecretConfig.GoogleOAuthSecretConfig::class
)
class OAuthSecretConfig(
    val kakaoOAuthSecretConfig: KakaoOAuthSecretConfig,
    val appleOAuthSecretConfig: AppleOAuthSecretConfig,
    val googleOAuthSecretConfig: GoogleOAuthSecretConfig,
) {
    init {
        val logger = KotlinLogging.logger { }
        OAuthSecretConfig::class.declaredMemberProperties
            .forEach { config ->
                logger.info { config.get(this).toString() }
            }
    }

    @ConfigurationProperties(prefix = "oauth.kakao")
    class KakaoOAuthSecretConfig(
        val clientId: String,
        val clientSecret: String,
        val adminKey: String,
    )

    @ConfigurationProperties(prefix = "oauth.apple")
    class AppleOAuthSecretConfig(
        val clientId: String,
        val webClientId: String,
        val keyId: String,
        val teamId: String,
        val authKey: String,
    )

    @ConfigurationProperties(prefix = "oauth.google")
    class GoogleOAuthSecretConfig(
        val clientId: String,
        val clientSecret: String,
    )
}

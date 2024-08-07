package com.oksusu.susu.client.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    OAuthUrlConfig.KakaoOAuthUrlConfig::class,
    OAuthUrlConfig.AppleOAuthUrlConfig::class,
    OAuthUrlConfig.GoogleOAuthUrlConfig::class
)
class OAuthUrlConfig(
    val kakaoOAuthUrlConfig: KakaoOAuthUrlConfig,
    val appleOAuthUrlConfig: AppleOAuthUrlConfig,
    val googleOAuthUrlConfig: GoogleOAuthUrlConfig,
) {
    init {
        val logger = KotlinLogging.logger { }
        OAuthUrlConfig::class.declaredMemberProperties
            .forEach { config ->
                logger.info { config.get(this).toString() }
            }
    }

    @ConfigurationProperties(prefix = "oauth-url.kakao")
    class KakaoOAuthUrlConfig(
        val withdrawCallbackUrl: String,
        val unlinkUrl: String,
        val userInfoUrl: String,
        val authorizeUrl: String,
        val tokenUrl: String,
        val kauthUrl: String,
        val kapiUrl: String,
        val redirectUrl: String,
    )

    @ConfigurationProperties(prefix = "oauth-url.apple")
    class AppleOAuthUrlConfig(
        val appleIdUrl: String,
        val devCallbackUrl: String,
        val withdrawCallbackUrl: String,
        val authorizeUrl: String,
        val tokenUrl: String,
        val oidcKeyUrl: String,
        val withdrawUrl: String,
    )

    @ConfigurationProperties(prefix = "oauth-url.google")
    class GoogleOAuthUrlConfig(
        val withdrawCallbackUrl: String,
        val revokeUrl: String,
        val userInfoUrl: String,
        val authorizeUrl: String,
        val tokenUrl: String,
        val accountGoogleUrl: String,
        val oauth2GoogleApiUrl: String,
        val googleApiUrl: String,
        val redirectUrl: String,
    )
}

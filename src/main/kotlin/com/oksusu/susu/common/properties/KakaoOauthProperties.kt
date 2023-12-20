package com.oksusu.susu.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.kakao")
class KakaoOauthProperties(
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

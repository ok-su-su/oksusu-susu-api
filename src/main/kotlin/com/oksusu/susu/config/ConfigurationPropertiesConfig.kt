package com.oksusu.susu.config

import com.oksusu.susu.common.properties.KakaoOauthProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KakaoOauthProperties::class)
class ConfigurationPropertiesConfig

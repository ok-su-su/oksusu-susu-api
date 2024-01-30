package com.oksusu.susu.slack.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    SlackAlarmConfig::class
)
class SlackConfig

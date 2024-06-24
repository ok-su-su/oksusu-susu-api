package com.oksusu.susu.client.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    SlackConfig.SlackWebhookConfig::class
)
class SlackConfig(
    val slcakWebhookConfig: SlackWebhookConfig,
) {
    init {
        val logger = KotlinLogging.logger { }
        SlackConfig::class.declaredMemberProperties
            .forEach { config ->
                logger.info { config.name }
            }
    }

    @ConfigurationProperties(prefix = "slack.webhook")
    class SlackWebhookConfig(
        val summaryToken: String,
        val errorToken: String,
    )
}

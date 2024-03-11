package com.oksusu.susu.api.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    SlackConfig.ErrorWebhookConfig::class
)
class SlackConfig(
    val errorWebhookConfig: ErrorWebhookConfig,
) {
    init {
        val logger = KotlinLogging.logger { }
        SlackConfig::class.declaredMemberProperties
            .forEach { config ->
                logger.info { config.name }
            }
    }

    sealed class SlackAlarmModel(
        open val url: String,
        open val text: String,
        open val userName: String,
    )

    @ConfigurationProperties(prefix = "slack.alarm.error-webhook")
    data class ErrorWebhookConfig(
        override val url: String,
        override val text: String,
        override val userName: String,
    ) : SlackAlarmModel(url, text, userName)
}

package com.oksusu.susu.client.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    DiscordConfig.DiscordWebhookConfig::class
)
class DiscordConfig(
    val discordWebhookConfig: DiscordWebhookConfig,
) {
    init {
        val logger = KotlinLogging.logger { }
        DiscordConfig::class.declaredMemberProperties
            .forEach { config ->
                logger.info { config.name }
            }
    }

    @ConfigurationProperties(prefix = "discord.webhook")
    class DiscordWebhookConfig(
        val summaryToken: String,
        val errorToken: String,
    )
}

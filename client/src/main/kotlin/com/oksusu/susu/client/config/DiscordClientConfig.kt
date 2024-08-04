package com.oksusu.susu.client.config

import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.client.WebClientFactory
import com.oksusu.susu.client.discord.DiscordClient
import com.oksusu.susu.client.discord.SuspendableDiscordClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordClientConfig(
    private val webhookConfig: DiscordConfig.DiscordWebhookConfig,
    private val cacheService: CacheService,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val DISCORD_WEBHOOKS_DOMAIN = "https://discord.com/api/webhooks"
    }

    @Bean
    fun discordClient(): DiscordClient {
        val webClient = WebClientFactory.generate(baseUrl = DISCORD_WEBHOOKS_DOMAIN)
        logger.info { "initialized discord client" }
        return SuspendableDiscordClient(webClient, webhookConfig, cacheService)
    }
}

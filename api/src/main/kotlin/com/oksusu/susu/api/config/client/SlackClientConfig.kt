package com.oksusu.susu.api.config.client

import com.oksusu.susu.api.client.WebClientFactory
import com.oksusu.susu.api.client.slack.SlackClient
import com.oksusu.susu.api.client.slack.SuspendableSlackClient
import com.oksusu.susu.api.config.SusuConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SlackClientConfig(
    private val webhookConfig: SusuConfig.SlackWebhookConfig,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val SLACK_WEBHOOKS_DOMAIN = "https://hooks.slack.com/services"
    }

    @Bean
    fun slackClient(): SlackClient {
        val webClient = WebClientFactory.generate(baseUrl = SLACK_WEBHOOKS_DOMAIN)
        logger.info { "initialized slack client" }
        return SuspendableSlackClient(webClient, webhookConfig)
    }
}

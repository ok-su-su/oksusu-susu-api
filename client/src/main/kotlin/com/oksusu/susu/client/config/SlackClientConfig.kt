package com.oksusu.susu.client.config

import com.oksusu.susu.client.WebClientFactory
import com.oksusu.susu.client.slack.SlackClient
import com.oksusu.susu.client.slack.SuspendableSlackClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SlackClientConfig(
    private val webhookConfig: SlackConfig.SlackWebhookConfig,
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

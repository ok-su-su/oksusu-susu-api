package com.oksusu.susu.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    SusuConfig.LedgerCreateFormConfig::class,
    SusuConfig.SlackWebhookConfig::class
)
data class SusuConfig(
    val ledgerCreateFormConfig: LedgerCreateFormConfig,
    val slackWebhookConfig: SlackWebhookConfig,
) {
    init {
        val logger = KotlinLogging.logger { }
        SusuConfig::class.declaredMemberProperties
            .forEach { config ->
                logger.info { config.get(this).toString() }
            }
    }

    @ConfigurationProperties(prefix = "susu.ledger-config.create-form")
    data class LedgerCreateFormConfig(
        val onlyStartAtCategoryIds: List<Long>,
    )

    @ConfigurationProperties(prefix = "slack")
    class SlackWebhookConfig(
        val token: String,
    )
}

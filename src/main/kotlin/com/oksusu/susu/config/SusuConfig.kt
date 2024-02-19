package com.oksusu.susu.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    SusuConfig.LedgerConfig::class,
    SusuConfig.SlackWebhookConfig::class,
    SusuConfig.OnboardingGetVoteConfig::class,
    SusuConfig.EnvelopeConfig::class,
    SusuConfig.CategoryConfig::class
)
data class SusuConfig(
    val ledgerConfig: LedgerConfig,
    val slackWebhookConfig: SlackWebhookConfig,
    val onboardingGetVoteConfig: OnboardingGetVoteConfig,
    val envelopeConfig: EnvelopeConfig,
    val categoryConfig: CategoryConfig,
) {
    init {
        val logger = KotlinLogging.logger { }
        SusuConfig::class.declaredMemberProperties
            .forEach { config ->
                logger.info { config.get(this).toString() }
            }
    }

    @ConfigurationProperties(prefix = "susu.ledger-config")
    data class LedgerConfig(
        val createForm: CreateForm,
    ) {
        data class CreateForm(
            val onlyStartAtCategoryIds: List<Long>,
            val minTitleLength: Int,
            val maxTitleLength: Int,
            val maxDescriptionLength: Int,
        )
    }

    @ConfigurationProperties(prefix = "slack")
    class SlackWebhookConfig(
        val token: String,
    )

    @ConfigurationProperties(prefix = "susu.onboarding-config.get-vote")
    data class OnboardingGetVoteConfig(
        val voteId: Long,
    )

    @ConfigurationProperties(prefix = "susu.envelop-config")
    data class EnvelopeConfig(
        val createForm: CreateForm,
    ) {
        data class CreateForm(
            val minAmount: Long,
            val maxAmount: Long,
            val maxGiftLength: Int,
            val maxMemoLength: Int,
        )
    }

    @ConfigurationProperties(prefix = "susu.category-config")
    data class CategoryConfig(
        val createForm: CreateForm,
    ) {
        data class CreateForm(
            val maxCustomCategoryLength: Int,
        )
    }
}

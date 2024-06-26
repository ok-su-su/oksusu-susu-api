package com.oksusu.susu.common.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    SusuConfig.LedgerConfig::class,
    SusuConfig.OnboardingGetVoteConfig::class,
    SusuConfig.EnvelopeConfig::class,
    SusuConfig.CategoryConfig::class,
    SusuConfig.PostConfig::class,
    SusuConfig.UserConfig::class,
    SusuConfig.StatisticConfig::class,
    SusuConfig.AdminUserConfig::class
)
data class SusuConfig(
    val ledgerConfig: LedgerConfig,
    val onboardingGetVoteConfig: OnboardingGetVoteConfig,
    val envelopeConfig: EnvelopeConfig,
    val categoryConfig: CategoryConfig,
    val postConfig: PostConfig,
    val userConfig: UserConfig,
    val adminUserConfig: AdminUserConfig,
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

    @ConfigurationProperties(prefix = "susu.post-config")
    data class PostConfig(
        val createForm: CreateForm,
        val createVoteForm: CreateVoteForm,
        val createVoteOptionForm: CreateVoteOptionForm,
    ) {
        data class CreateForm(
            val maxTitleLength: Int,
            val minContentLength: Int,
            val maxContentLength: Int,
        )

        data class CreateVoteForm(
            val minOptionCount: Int,
        )

        data class CreateVoteOptionForm(
            val minContentLength: Int,
            val maxContentLength: Int,
        )
    }

    @ConfigurationProperties(prefix = "susu.user-config")
    data class UserConfig(
        val createForm: CreateForm,
    ) {
        data class CreateForm(
            val minBirthYear: Int,
            val minNameLength: Int,
            val maxNameLength: Int,
        )
    }

    @ConfigurationProperties(prefix = "susu.statistic-config")
    data class StatisticConfig(
        val susuEnvelopeConfig: SusuEnvelopeConfig,
    ) {
        data class SusuEnvelopeConfig(
            val minCuttingAverage: Float,
            val maxCuttingAverage: Float,
        )
    }

    @ConfigurationProperties(prefix = "susu.admin-user-config")
    data class AdminUserConfig(
        val adminUserUid: List<Long>,
    )
}

package com.oksusu.susu.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    SusuConfig.LedgerCreateFormConfig::class
)
class SusuConfig {
    init {
        val logger = mu.KotlinLogging.logger { }
        SusuConfig::class.declaredMemberProperties
            .forEach { config -> logger.info(config.get(this).toString()) }
    }

    @ConfigurationProperties("susu.ledger-config.create-form")
    data class LedgerCreateFormConfig(
        val onlyStartAtCategoryIds: List<Long>,
    )
}

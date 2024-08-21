package com.oksusu.susu.api.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.declaredMemberProperties

@Configuration
@EnableConfigurationProperties(
    LockConfig.ActorLockConfig::class,
)
class LockConfig(
    val actorLockConfig: ActorLockConfig,
) {
    init {
        val logger = KotlinLogging.logger { }
        LockConfig::class.declaredMemberProperties
            .forEach { config ->
                logger.info { config.get(this).toString() }
            }
    }

    @ConfigurationProperties(prefix = "lock.actor-lock")
    class ActorLockConfig(
        val waitTimeMilli: Long,
        val leaseTimeMilli: Long,
    )
}

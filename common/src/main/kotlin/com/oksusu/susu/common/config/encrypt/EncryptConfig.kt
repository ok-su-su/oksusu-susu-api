package com.oksusu.susu.common.config.encrypt

import com.oksusu.susu.common.encrypt.Encryptor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(EncryptConfig.EncryptProperty::class)
class EncryptConfig {
    private val logger = KotlinLogging.logger {}

    @ConfigurationProperties(prefix = "encrypt")
    data class EncryptProperty(
        val key: String,
        val algorithm: String,
    )

    @Bean
    fun encryptor(
        property: EncryptProperty,
    ): Encryptor {
        logger.info { "initialized encryptor. key: ${property.key} algorithm: ${property.algorithm}" }
        return Encryptor(property.key, property.algorithm)
    }
}

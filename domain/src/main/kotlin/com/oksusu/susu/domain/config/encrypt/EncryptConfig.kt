package com.oksusu.susu.domain.config.encrypt

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "encrypt")
@ConfigurationPropertiesBinding
data class EncryptConfig(
    @field:NotBlank
    var key: String = "",

    @field:NotBlank
    var algorithm: String = "",
)

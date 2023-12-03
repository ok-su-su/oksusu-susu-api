package com.oksusu.susu.config.jwt

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "auth.jwt")
@ConfigurationPropertiesBinding
data class JwtConfig(
    @field:NotBlank
    var secretKey: String = "",
    val issuer: String = "oksusu-susu-api",
    val audience: String = "oksusu-susu-api",
)

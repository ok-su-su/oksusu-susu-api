package com.oksusu.susu.api.config.web

import com.oksusu.susu.api.auth.model.AUTH_TOKEN_KEY
import com.oksusu.susu.api.auth.model.AdminUser
import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.user.model.UserDeviceContext
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.result.view.RequestContext
import org.springframework.web.server.WebSession

/**
 * **Swagger**
 *
 * Provide detailed explanations based on comments.
 *
 * [Local Swagger UI](http://localhost:8080/webjars/swagger-ui/index.html)
 */
@Configuration
class SpringDocConfig(
    private val buildProperties: BuildProperties,
) {
    init {
        SpringDocUtils
            .getConfig()
            .addRequestWrapperToIgnore(
                UserDeviceContext::class.java,
                AuthUser::class.java,
                AdminUser::class.java,
                WebSession::class.java,
                RequestContext::class.java
            )
    }

    @Bean
    fun openApi(): OpenAPI {
        val securityRequirement = SecurityRequirement().addList(AUTH_TOKEN_KEY)
        return OpenAPI()
            .components(authSetting())
            .security(listOf(securityRequirement))
            .addServersItem(Server().url("/"))
            .info(
                Info()
                    .title(buildProperties.name)
                    .version(buildProperties.version)
                    .description("Susu Rest API Docs")
            )
    }

    private fun authSetting(): Components {
        return Components()
            .addSecuritySchemes(
                AUTH_TOKEN_KEY,
                SecurityScheme()
                    .description("Access Token")
                    .type(SecurityScheme.Type.APIKEY)
                    .`in`(SecurityScheme.In.HEADER)
                    .name(AUTH_TOKEN_KEY)
            )
    }
}

object SwaggerTag {
    const val USER_SWAGGER_TAG = "User API"
    const val TERM_SWAGGER_TAG = "Term API"
    const val STATISTIC_SWAGGER_TAG = "Statistic API"
    const val AUTH_SWAGGER_TAG = "Auth API"
    const val OAUTH_SWAGGER_TAG = "OAuth API"
    const val CATEGORY_SWAGGER_TAG = "Category API"
    const val DEV_SWAGGER_TAG = "DEV API"
    const val DEV_OAUTH_SWAGGER_TAG = "DEV OAuth API"
    const val DEV_BATCH_SWAGGER_TAG = "DEV Batch API"
    const val ENVELOPE_CONFIG_SWAGGER_TAG = "Envelope Config API"
    const val ENVELOPE_SWAGGER_TAG = "Envelope API"
    const val FRIEND_SWAGGER_TAG = "Friend API"
    const val HEALTH_SWAGGER_TAG = "Health API"
    const val LEDGER_SWAGGER_TAG = "Ledger API"
    const val LEDGER_CONFIG_SWAGGER_TAG = "Ledger Config API"
    const val POST_CONFIG_SWAGGER_TAG = "Post Config API"
    const val VOTE_SWAGGER_TAG = "Vote API"
    const val EXCEL_SWAGGER_TAG = "Excel API"
    const val BLOCK_SWAGGER_TAG = "Block API"
    const val DEV_BLOCK_SWAGGER_TAG = "DEV Block API"
    const val REPORT_SWAGGER_TAG = "Report API"
    const val ONBOARDING_SWAGGER_TAG = "Onboarding API"
    const val APPLICATION_METADATA_SWAGGER_TAG = "Application Metadata API"
    const val ADMIN_VOTE_SWAGGER_TAG = "Admin Vote API"
    const val ADMIN_SWAGGER_TAG = "Admin API"
}

package com.oksusu.susu.api.health.presentation

import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.api.extension.wrapOk
import com.oksusu.susu.api.health.model.HealthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.HEALTH_SWAGGER_TAG, description = "Custom Health Check")
@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class HealthResource(
    private val environment: Environment,
) {
    @Operation(summary = "health-check-v1")
    @GetMapping("/api/v1/health-check")
    fun healthCheckV1() = environment.activeProfiles.first()
        .run { HealthResponse.from(this) }
        .wrapOk()
}

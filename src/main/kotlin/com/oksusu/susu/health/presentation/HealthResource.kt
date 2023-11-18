package com.oksusu.susu.health.presentation

import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.health.dto.HealthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@Tag(name = "Health")
@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class HealthResource(
    private val environment: Environment,
) {
    @Operation(summary = "health check")
    @GetMapping("/health")
    fun health() = HealthResponse(
        message = "health good~!",
        dateTime = LocalDateTime.now(),
        profile = environment.activeProfiles.contentToString()
    ).wrapOk()
}

package com.oksusu.susu.health.presentation

import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.health.dto.HealthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@Tag(name = SwaggerTag.HEALTH_SWAGGER_TAG, description = "Health API")
@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class HealthResource(
    private val environment: Environment,
) {
    /** health check */
    @Operation(summary = "health check")
    @GetMapping("/health")
    suspend fun health(): ResponseEntity<HealthResponse> {
        withContext(Dispatchers.IO) {
            println("hello")
        }
        withContext(Dispatchers.IO) {
            throw Exception("goofy")
        }






        return HealthResponse(
            message = "health good~!",
            dateTime = LocalDateTime.now(),
            profile = environment.activeProfiles.contentToString()
        ).wrapOk()
    }
}

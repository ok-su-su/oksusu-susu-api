package com.oksusu.susu.api.metadata.presentation

import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.common.extension.wrapOk
import com.oksusu.susu.api.metadata.application.ApplicationMetadataFacade
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.APPLICATION_METADATA_SWAGGER_TAG, description = "어플리케이션 설정 정보 API")
@RestController
@RequestMapping(value = ["/api/v1/metadata"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ApplicationMetadataResource(
    private val applicationMetadataFacade: ApplicationMetadataFacade,
) {
    @Operation(summary = "버전 설정 정보 조회")
    @GetMapping("/version")
    suspend fun getApplicationVersionMetadata() = applicationMetadataFacade.getApplicationVersionMetadata().wrapOk()
}

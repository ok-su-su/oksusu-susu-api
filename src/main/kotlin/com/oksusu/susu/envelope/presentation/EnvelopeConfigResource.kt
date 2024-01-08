package com.oksusu.susu.envelope.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.envelope.application.EnvelopeConfigService
import com.oksusu.susu.extension.wrapOk
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.ENVELOPE_CONFIG_SWAGGER_TAG)
@RestController
@RequestMapping(value = ["/api/v1/envelopes/configs"], produces = [MediaType.APPLICATION_JSON_VALUE])
class EnvelopeConfigResource(
    private val envelopeConfigService: EnvelopeConfigService,
) {
    @Operation(summary = "봉투 생성 config 데이터 제공")
    @GetMapping("/create-envelopes")
    suspend fun getCreateEnvelopesConfig(
        user: AuthUser,
    ) = envelopeConfigService.getCreateEnvelopesConfig(user).wrapOk()
}

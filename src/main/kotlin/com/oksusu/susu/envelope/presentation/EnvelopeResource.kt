package com.oksusu.susu.envelope.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.model.request.CreateEnvelopeRequest
import com.oksusu.susu.extension.wrapCreated
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "봉투")
@RestController
@RequestMapping(value = ["/api/v1/envelopes"], produces = [MediaType.APPLICATION_JSON_VALUE])
class EnvelopeResource(
    private val envelopeService: EnvelopeService,
) {
    @Operation(summary = "생성")
    @PostMapping
    suspend fun create(
        user: AuthUser,
        @RequestBody request: CreateEnvelopeRequest,
    ) = envelopeService.create(user, request).wrapCreated()
}

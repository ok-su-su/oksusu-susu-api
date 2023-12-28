package com.oksusu.susu.envelope.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.envelope.application.EnvelopeFacade
import com.oksusu.susu.envelope.model.request.CreateEnvelopeRequest
import com.oksusu.susu.extension.wrapCreated
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.extension.wrapVoid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "봉투")
@RestController
@RequestMapping(value = ["/api/v1/envelopes"], produces = [MediaType.APPLICATION_JSON_VALUE])
class EnvelopeResource(
    private val envelopeFacade: EnvelopeFacade,
) {
    @Operation(summary = "생성")
    @PostMapping
    suspend fun create(
        user: AuthUser,
        @RequestBody request: CreateEnvelopeRequest,
    ) = envelopeFacade.create(user, request).wrapCreated()

    @Operation(summary = "상세조회")
    @GetMapping("/{id}")
    suspend fun get(
        user: AuthUser,
        @PathVariable id: Long,
    ) = envelopeFacade.getDetail(user, id).wrapOk()

    @Operation(summary = "삭제")
    @DeleteMapping("/{id}")
    suspend fun delete(
        user: AuthUser,
        @PathVariable id: Long,
    ) = envelopeFacade.delete(user, id).wrapVoid()
}

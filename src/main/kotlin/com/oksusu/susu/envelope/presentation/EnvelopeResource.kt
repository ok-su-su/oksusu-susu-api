package com.oksusu.susu.envelope.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.envelope.application.EnvelopeFacade
import com.oksusu.susu.envelope.model.request.CreateAndUpdateEnvelopeRequest
import com.oksusu.susu.envelope.model.request.SearchEnvelopeRequest
import com.oksusu.susu.envelope.model.request.SearchFriendStatisticsRequest
import com.oksusu.susu.extension.wrapCreated
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.extension.wrapPage
import com.oksusu.susu.extension.wrapVoid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.ENVELOPE_SWAGGER_TAG)
@RestController
@RequestMapping(value = ["/api/v1/envelopes"], produces = [MediaType.APPLICATION_JSON_VALUE])
class EnvelopeResource(
    private val envelopeFacade: EnvelopeFacade,
) {
    @Operation(summary = "생성")
    @PostMapping
    suspend fun create(
        user: AuthUser,
        @RequestBody request: CreateAndUpdateEnvelopeRequest,
    ) = envelopeFacade.create(user, request).wrapCreated()

    @Operation(summary = "수정")
    @PatchMapping("/{id}")
    suspend fun update(
        user: AuthUser,
        @PathVariable id: Long,
        @RequestBody request: CreateAndUpdateEnvelopeRequest,
    ) = envelopeFacade.update(
        user = user,
        id = id,
        request = request
    ).wrapOk()

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

    @Operation(summary = "검색")
    @GetMapping
    suspend fun search(
        user: AuthUser,
        @ParameterObject request: SearchEnvelopeRequest,
        @ParameterObject pageRequest: SusuPageRequest,
    ) = envelopeFacade.search(
        user = user,
        request = request,
        pageRequest = pageRequest
    ).wrapPage()

    @Operation(summary = "친구 봉투 통계 조회")
    @GetMapping("/friend-statistics")
    suspend fun searchFriendStatistics(
        user: AuthUser,
        @ParameterObject request: SearchFriendStatisticsRequest,
        @ParameterObject pageRequest: SusuPageRequest,
    ) = envelopeFacade.searchFriendStatistics(
        user = user,
        request = request,
        pageRequest = pageRequest
    ).wrapPage()
}

package com.oksusu.susu.api.envelope.presentation

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.domain.common.dto.SusuPageRequest
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.api.envelope.application.LedgerFacade
import com.oksusu.susu.api.envelope.model.request.CreateAndUpdateLedgerRequest
import com.oksusu.susu.api.envelope.model.request.SearchLedgerRequest
import com.oksusu.susu.domain.common.extension.wrapCreated
import com.oksusu.susu.domain.common.extension.wrapOk
import com.oksusu.susu.domain.common.extension.wrapPage
import com.oksusu.susu.domain.common.extension.wrapVoid
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.LEDGER_SWAGGER_TAG, description = "장부 관리")
@RestController
@RequestMapping(value = ["/api/v1/ledgers"], produces = [MediaType.APPLICATION_JSON_VALUE])
class LedgerResource(
    private val ledgerFacade: LedgerFacade,
) {
    @Operation(summary = "장부 생성")
    @PostMapping
    suspend fun create(
        user: AuthUser,
        @RequestBody request: CreateAndUpdateLedgerRequest,
    ) = ledgerFacade.create(user, request).wrapCreated()

    @Operation(summary = "장부 수정")
    @PatchMapping("/{id}")
    suspend fun update(
        user: AuthUser,
        @PathVariable id: Long,
        @RequestBody request: CreateAndUpdateLedgerRequest,
    ) = ledgerFacade.update(
        user = user,
        id = id,
        request = request
    ).wrapOk()

    @Operation(summary = "장부 조회")
    @GetMapping("/{id}")
    suspend fun get(
        user: AuthUser,
        @PathVariable id: Long,
    ) = ledgerFacade.get(user, id).wrapOk()

    /**
     * **검색조건**
     * - title, categoryIds, fromStartAT, toStartAt 모두 현재 조건상 nullable
     *
     * **검색 정렬 조건**
     * - createdAt (생성)
     * - totalSentAmounts (보낸 금액 총합)
     * - totalReceivedAmounts (받은 금액 총합)
     */
    @Operation(summary = "장부 검색")
    @GetMapping
    suspend fun search(
        user: AuthUser,
        @ParameterObject request: SearchLedgerRequest,
        @ParameterObject pageRequest: SusuPageRequest,
    ) = ledgerFacade.search(
        user = user,
        request = request,
        pageRequest = pageRequest
    ).wrapPage()

    @Operation(summary = "장부 삭제")
    @DeleteMapping
    suspend fun delete(
        user: AuthUser,
        @RequestParam ids: Set<Long>,
    ) = ledgerFacade.delete(user, ids).wrapVoid()
}

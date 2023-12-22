package com.oksusu.susu.ledger.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.extension.wrapCreated
import com.oksusu.susu.extension.wrapPage
import com.oksusu.susu.extension.wrapVoid
import com.oksusu.susu.ledger.application.LedgerService
import com.oksusu.susu.ledger.model.request.CreateLedgerRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "장부")
@RestController
@RequestMapping(value = ["/api/v1/ledgers"], produces = [MediaType.APPLICATION_JSON_VALUE])
class LedgerResource(
    private val ledgerService: LedgerService,
) {
    @Operation(summary = "장부 생성")
    @PostMapping
    suspend fun create(
        user: AuthUser,
        @RequestBody request: CreateLedgerRequest,
    ) = ledgerService.create(user, request).wrapCreated()

    @Operation(summary = "장부 검색")
    @GetMapping
    suspend fun getAll(
        user: AuthUser,
        @ParameterObject pageRequest: SusuPageRequest,
    ) = ledgerService.getAll(user, pageRequest).wrapPage()

    @Operation(summary = "장부 삭제")
    @DeleteMapping
    suspend fun delete(
        user: AuthUser,
        @RequestParam ids: Set<Long>,
    ) = ledgerService.delete(user, ids).wrapVoid()
}

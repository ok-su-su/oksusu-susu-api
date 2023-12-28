package com.oksusu.susu.term.presentation

import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.term.application.TermFacade
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "약관")
@RestController
@RequestMapping(value = ["/api/v1/terms"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TermResource(
    private val termFacade: TermFacade,
) {
    @Operation(summary = "약관 정보 조회")
    @GetMapping
    suspend fun getTermInfos() = termFacade.getTermInfos().wrapOk()

    @Operation(summary = "약관 조회")
    @GetMapping("/{id}")
    suspend fun getTerm(
        @PathVariable id: Long,
    ) = termFacade.getTerm(id).wrapOk()
}

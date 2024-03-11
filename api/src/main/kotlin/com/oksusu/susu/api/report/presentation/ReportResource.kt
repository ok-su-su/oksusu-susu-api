package com.oksusu.susu.api.report.presentation

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.common.extension.wrapCreated
import com.oksusu.susu.api.report.application.ReportFacade
import com.oksusu.susu.domain.report.domain.vo.ReportTargetType
import com.oksusu.susu.api.report.model.request.ReportCreateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.REPORT_SWAGGER_TAG, description = "리포트 관리 API")
@RestController
@RequestMapping(value = ["/api/v1/reports"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ReportResource(
    private val reportFacade: ReportFacade,
) {
    @Operation(summary = "신고 메타데이터 조회")
    @GetMapping("/metadata")
    suspend fun getMetadata(
        @RequestParam targetType: ReportTargetType,
    ) = reportFacade.getAllMetadata(targetType)

    @Operation(summary = "신고하기")
    @PostMapping
    suspend fun report(
        user: AuthUser,
        @RequestBody request: ReportCreateRequest,
    ) = reportFacade.report(user, request).wrapCreated()
}

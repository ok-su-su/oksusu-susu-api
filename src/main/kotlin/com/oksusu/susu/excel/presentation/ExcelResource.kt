package com.oksusu.susu.excel.presentation

import com.mysema.commons.lang.URLEncoder
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.excel.application.ExcelFacade
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.buffer.DefaultDataBuffer
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Excel")
@RestController
@RequestMapping(value = ["/api/v1/excel"])
class ExcelResource(
    private val excelFacade: ExcelFacade,
) {
    companion object {
        const val EXCEL_TITLE = "수수_기록.xlsx"
    }

    @Operation(summary = "excel 파일로 가져오기")
    @GetMapping
    suspend fun getExcel(
        user: AuthUser,
    ): ResponseEntity<DefaultDataBuffer> {
        val fileName = URLEncoder.encodeParam(EXCEL_TITLE, "UTF-8")

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=$fileName")
            .header("Content-type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .body(excelFacade.getExcel(user))
    }
}

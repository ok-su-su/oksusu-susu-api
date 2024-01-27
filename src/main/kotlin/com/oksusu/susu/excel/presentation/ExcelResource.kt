package com.oksusu.susu.excel.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.excel.application.ExcelFacade
import com.oksusu.susu.extension.encodeURL
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.buffer.DefaultDataBuffer
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.EXCEL_SWAGGER_TAG, description = "Excel API")
@RestController
@RequestMapping(value = ["/api/v1/excel"])
class ExcelResource(
    private val excelFacade: ExcelFacade,
) {
    companion object {
        const val DEFAULT_EXCEL_TITLE = "수수_기록"
    }

    @Operation(summary = "excel 파일로 가져오기")
    @GetMapping("/all-envelopes")
    suspend fun getAllEnvelopsExcel(
        user: AuthUser,
        @RequestParam(defaultValue = DEFAULT_EXCEL_TITLE) fileName: String,
    ): ResponseEntity<DefaultDataBuffer> {
        val encodedFileName = "$fileName.xlsx".encodeURL()

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=$encodedFileName")
            .header("Content-type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .body(excelFacade.getAllEnvelopsExcel(user))
    }
}

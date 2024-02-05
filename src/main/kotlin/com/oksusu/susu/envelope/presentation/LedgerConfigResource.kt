package com.oksusu.susu.envelope.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.envelope.application.LedgerConfigService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.LEDGER_CONFIG_SWAGGER_TAG, description = "장부 관련 config api")
@RestController
@RequestMapping(value = ["/api/v1/ledgers/configs"], produces = [MediaType.APPLICATION_JSON_VALUE])
class LedgerConfigResource(
    private val ledgerConfigService: LedgerConfigService,
) {
    /**
     * **장부 생성 config**
     * - 장부 생성시, 시작일 정보만 필요한 categoryIds
     */
    @Operation(summary = "장부 생성 config", description = "장부 생성시 필요한 config 데이터 제공")
    @GetMapping("/create-ledger")
    suspend fun getCreateLedgerConfig(user: AuthUser) = ledgerConfigService.getCreateLedgerConfig(user).wrapOk()
}

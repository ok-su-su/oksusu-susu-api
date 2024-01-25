package com.oksusu.susu.ledger.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.ledger.application.LedgerConfigService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.LEDGER_CONFIG_SWAGGER_TAG)
@RestController
@RequestMapping(value = ["/api/v1/ledgers/configs"], produces = [MediaType.APPLICATION_JSON_VALUE])
class LedgerConfigResource(
    private val ledgerConfigService: LedgerConfigService,
) {
    @Operation(summary = "장부 생성 config")
    @GetMapping("/create-ledger")
    suspend fun getCreateLedgerConfig(user: AuthUser) = ledgerConfigService.getCreateLedgerConfig(user).wrapOk()
}

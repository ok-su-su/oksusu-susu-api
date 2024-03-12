package com.oksusu.susu.api.envelope.application

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.config.SusuApiConfig
import com.oksusu.susu.api.envelope.model.response.CreateLedgerConfigResponse
import org.springframework.stereotype.Service

@Service
class LedgerConfigService(
    private val ledgerConfig: SusuApiConfig.LedgerConfig,
) {
    suspend fun getCreateLedgerConfig(user: AuthUser): CreateLedgerConfigResponse {
        return CreateLedgerConfigResponse(
            onlyStartAtCategoryIds = ledgerConfig.createForm.onlyStartAtCategoryIds
        )
    }
}

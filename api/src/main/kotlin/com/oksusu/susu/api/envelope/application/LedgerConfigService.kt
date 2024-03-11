package com.oksusu.susu.api.envelope.application

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.config.SusuConfig
import com.oksusu.susu.api.envelope.model.response.CreateLedgerConfigResponse
import org.springframework.stereotype.Service

@Service
class LedgerConfigService(
    private val ledgerConfig: SusuConfig.LedgerConfig,
) {
    suspend fun getCreateLedgerConfig(user: AuthUser): CreateLedgerConfigResponse {
        return CreateLedgerConfigResponse(
            onlyStartAtCategoryIds = ledgerConfig.createForm.onlyStartAtCategoryIds
        )
    }
}

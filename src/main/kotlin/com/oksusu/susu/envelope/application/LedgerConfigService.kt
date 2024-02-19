package com.oksusu.susu.envelope.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.SusuConfig
import com.oksusu.susu.envelope.model.response.CreateLedgerConfigResponse
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

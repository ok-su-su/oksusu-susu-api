package com.oksusu.susu.ledger.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.SusuConfig
import com.oksusu.susu.post.model.response.CreateLedgerConfigResponse
import org.springframework.stereotype.Service

@Service
class LedgerConfigService(
    private val ledgerCreateFormConfig: SusuConfig.LedgerCreateFormConfig,
) {
    suspend fun getCreateLedgerConfig(user: AuthUser): CreateLedgerConfigResponse {
        return CreateLedgerConfigResponse(
            onlyStartAtCategoryIds = ledgerCreateFormConfig.onlyStartAtCategoryIds
        )
    }
}

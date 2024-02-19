package com.oksusu.susu.envelope.application

import com.oksusu.susu.config.SusuConfig
import com.oksusu.susu.envelope.model.request.CreateAndUpdateLedgerRequest
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import org.springframework.stereotype.Service

@Service
class LedgerValidateService(
    private val ledgerConfig: SusuConfig.LedgerConfig,
    private val categoryConfig: SusuConfig.CategoryConfig,
) {
    fun validateLedgerRequest(request: CreateAndUpdateLedgerRequest) {
        if (request.startAt.isAfter(request.endAt)) {
            throw InvalidRequestException(ErrorCode.LEDGER_INVALID_DUE_DATE_ERROR)
        }

        val ledgerCreateForm = ledgerConfig.createForm
        val categoryCreateForm = categoryConfig.createFrom

        if (request.title.length !in ledgerCreateForm.minTitleLength..ledgerCreateForm.maxTitleLength) {
            throw InvalidRequestException(ErrorCode.INVALID_LEDGER_TITLE_ERROR)
        }

        if (request.description != null && request.description.length > ledgerCreateForm.maxDescriptionLength) {
            throw InvalidRequestException(ErrorCode.INVALID_LEDGER_DESCRIPTION_ERROR)
        }

        if (
            request.customCategory != null &&
            request.customCategory.length >= categoryCreateForm.maxCustomCategoryLength
        ) {
            throw InvalidRequestException(ErrorCode.INVALID_CUSTOM_CATEGORY_ERROR)
        }
    }
}

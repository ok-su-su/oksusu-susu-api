package com.oksusu.susu.envelope.application

import com.oksusu.susu.config.SusuConfig
import com.oksusu.susu.envelope.model.request.CreateAndUpdateEnvelopeRequest
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import org.springframework.stereotype.Service

@Service
class EnvelopeValidateService(
    private val envelopeConfig: SusuConfig.EnvelopeConfig,
    private val categoryConfig: SusuConfig.CategoryConfig,
) {
    fun validateEnvelopeRequest(request: CreateAndUpdateEnvelopeRequest) {
        val envelopeCreateForm = envelopeConfig.createForm
        val categoryCreateForm = categoryConfig.createForm

        if (request.amount !in envelopeCreateForm.minAmount..envelopeCreateForm.maxAmount) {
            throw InvalidRequestException(ErrorCode.INVALID_ENVELOPE_AMOUNT_ERROR)
        }

        if (request.gift != null && request.gift.length >= envelopeCreateForm.maxGiftLength) {
            throw InvalidRequestException(ErrorCode.INVALID_ENVELOPE_GIFT_ERROR)
        }

        if (request.memo != null && request.memo.length >= envelopeCreateForm.maxMemoLength) {
            throw InvalidRequestException(ErrorCode.INVALID_ENVELOPE_MEMO_ERROR)
        }

        if (
            request.category?.customCategory != null &&
            request.category.customCategory.length >= categoryCreateForm.maxCustomCategoryLength
        ) {
            throw InvalidRequestException(ErrorCode.INVALID_CUSTOM_CATEGORY_ERROR)
        }
    }
}

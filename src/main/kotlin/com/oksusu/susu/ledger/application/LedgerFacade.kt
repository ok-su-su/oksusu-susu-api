package com.oksusu.susu.ledger.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryAssignmentService
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.category.domain.CategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.ledger.domain.Ledger
import com.oksusu.susu.ledger.model.request.CreateLedgerRequest
import com.oksusu.susu.ledger.model.response.CreateLedgerResponse
import org.springframework.stereotype.Service

@Service
class LedgerFacade(
    private val ledgerService: LedgerService,
    private val categoryService: CategoryService,
    private val categoryAssignmentService: CategoryAssignmentService,
    private val txTemplate: TransactionTemplates,
) {
    suspend fun create(user: AuthUser, request: CreateLedgerRequest): CreateLedgerResponse {
        if (request.startAt.isAfter(request.endAt)) {
            throw InvalidRequestException(ErrorCode.LEDGER_INVALID_DUE_DATE_ERROR)
        }

        val category = categoryService.getCategory(request.categoryId)

        /** 기타 항목인 경우에만 커스텀 카테고리를 생성한다. */
        val customCategory = when (category.id == 5L) {
            true -> request.customCategory
            else -> null
        }

        val createdLedger = txTemplate.writer.executeWithContext {
            val createdLedger = Ledger(
                uid = user.id,
                title = request.title,
                description = request.description,
                startAt = request.startAt,
                endAt = request.endAt
            ).run { ledgerService.saveSync(this) }

            CategoryAssignment(
                targetId = createdLedger.id,
                targetType = CategoryAssignmentType.LEDGER,
                categoryId = category.id,
                customCategory = customCategory
            ).run { categoryAssignmentService.saveSync(this) }

            createdLedger
        } ?: throw FailToCreateException(ErrorCode.FAIL_TO_CREATE_LEDGER_ERROR)

        return CreateLedgerResponse.of(
            ledger = createdLedger,
            category = category,
            customCategory = customCategory
        )
    }
}

package com.oksusu.susu.ledger.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryAssignmentService
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.category.domain.CategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.category.model.CategoryWithCustomModel
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.event.model.DeleteLedgerEvent
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.ledger.domain.Ledger
import com.oksusu.susu.ledger.infrastructure.model.SearchLedgerSpec
import com.oksusu.susu.ledger.model.LedgerModel
import com.oksusu.susu.ledger.model.request.CreateLedgerRequest
import com.oksusu.susu.ledger.model.request.SearchLedgerRequest
import com.oksusu.susu.ledger.model.response.CreateLedgerResponse
import com.oksusu.susu.ledger.model.response.SearchLedgerResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class LedgerFacade(
    private val ledgerService: LedgerService,
    private val envelopeService: EnvelopeService,
    private val categoryService: CategoryService,
    private val categoryAssignmentService: CategoryAssignmentService,
    private val txTemplate: TransactionTemplates,
    private val publisher: ApplicationEventPublisher,
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

    suspend fun search(
        user: AuthUser,
        request: SearchLedgerRequest,
        pageRequest: SusuPageRequest,
    ): Page<SearchLedgerResponse> {
        val searchSpec = SearchLedgerSpec(
            uid = user.id,
            categoryId = request.categoryId,
            fromStartAt = request.fromStartAt,
            toStartAt = request.toEndAt
        )

        val response = ledgerService.search(searchSpec, pageRequest.toDefault())

        val statistics = response.content.map { (ledger, _) -> ledger.id }
            .run { envelopeService.countTotalAmountsAndCounts(this) }
            .associateBy { ledger -> ledger.ledgerId }

        return response.map { (ledger, categoryAssignment) ->
            val category = categoryService.getCategory(categoryAssignment.categoryId)
            val statistic = statistics[ledger.id]

            SearchLedgerResponse(
                ledger = LedgerModel.from(ledger),
                category = CategoryWithCustomModel.of(category, categoryAssignment.customCategory),
                totalAmounts = statistic?.totalAmounts ?: 0L,
                totalCounts = statistic?.totalCounts ?: 0L
            )
        }
    }

    suspend fun delete(user: AuthUser, ids: Set<Long>) {
        val ledgers = ledgerService.findAllByUidAndIdIn(user.id, ids.toList())

        ledgers.forEach { leder ->
            txTemplate.writer.executeWithContext {
                /** 장부 삭제 */
                ledgerService.deleteSync(leder)

                /** 카테고리 정보 삭제 */
                categoryAssignmentService.deleteByTargetIdAndTargetTypeSync(
                    targetId = leder.id,
                    targetType = CategoryAssignmentType.LEDGER
                )

                /** 봉투 삭제 */
                envelopeService.deleteAllByLedgerId(leder.id)

                /** 이벤트 발행 */
                publisher.publishEvent(DeleteLedgerEvent(leder))
            }
        }
    }
}

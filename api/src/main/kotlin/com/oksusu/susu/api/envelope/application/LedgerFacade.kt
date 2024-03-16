package com.oksusu.susu.api.envelope.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.category.application.CategoryAssignmentService
import com.oksusu.susu.api.category.application.CategoryService
import com.oksusu.susu.api.category.model.CategoryWithCustomModel
import com.oksusu.susu.api.common.dto.SusuPageRequest
import com.oksusu.susu.api.envelope.model.LedgerModel
import com.oksusu.susu.api.envelope.model.request.CreateAndUpdateLedgerRequest
import com.oksusu.susu.api.envelope.model.request.SearchLedgerRequest
import com.oksusu.susu.api.envelope.model.response.CreateAndUpdateLedgerResponse
import com.oksusu.susu.api.envelope.model.response.LedgerDetailResponse
import com.oksusu.susu.api.envelope.model.response.SearchLedgerResponse
import com.oksusu.susu.api.event.model.DeleteLedgerEvent
import com.oksusu.susu.domain.category.domain.CategoryAssignment
import com.oksusu.susu.domain.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.domain.common.extension.coExecute
import com.oksusu.susu.domain.common.extension.coExecuteOrNull
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.domain.envelope.domain.Ledger
import com.oksusu.susu.domain.envelope.infrastructure.model.SearchLedgerSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
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
    private val ledgerValidateService: LedgerValidateService,
) {
    suspend fun create(user: AuthUser, request: CreateAndUpdateLedgerRequest): CreateAndUpdateLedgerResponse {
        ledgerValidateService.validateLedgerRequest(request)

        val category = categoryService.getCategory(request.categoryId)

        /** 기타 항목인 경우에만 커스텀 카테고리를 생성한다. */
        val customCategory = when (category.id == 5L) {
            true -> request.customCategory
            else -> null
        }

        val createdLedger = txTemplate.writer.coExecute(Dispatchers.IO + MDCContext()) {
            val createdLedger = Ledger(
                uid = user.uid,
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
        }

        return CreateAndUpdateLedgerResponse.of(
            ledger = createdLedger,
            category = category,
            customCategory = customCategory
        )
    }

    suspend fun update(
        user: AuthUser,
        id: Long,
        request: CreateAndUpdateLedgerRequest,
    ): CreateAndUpdateLedgerResponse? {
        ledgerValidateService.validateLedgerRequest(request)

        val (ledger, categoryAssignment) = ledgerService.findLedgerDetailOrThrow(id, user.uid)

        val category = categoryService.getCategory(request.categoryId)

        /** 기타 항목인 경우에만 커스텀 카테고리를 생성한다. */
        val customCategory = when (category.id == 5L) {
            true -> request.customCategory
            else -> null
        }

        val updatedLedger = txTemplate.writer.coExecute(Dispatchers.IO + MDCContext()) {
            val updatedLedger = ledger.apply {
                this.title = request.title
                this.description = request.description
                this.startAt = request.startAt
                this.endAt = request.endAt
            }.run { ledgerService.saveSync(this) }

            categoryAssignment.apply {
                this.categoryId = category.id
                this.customCategory = customCategory
            }.run { categoryAssignmentService.saveSync(this) }

            updatedLedger
        }

        return CreateAndUpdateLedgerResponse.of(
            ledger = updatedLedger,
            category = category,
            customCategory = customCategory
        )
    }

    suspend fun get(user: AuthUser, id: Long): LedgerDetailResponse {
        val (ledger, categoryAssignment) = ledgerService.findLedgerDetailOrThrow(id, user.uid)

        return parZip(
            { categoryService.getCategory(categoryAssignment.categoryId) },
            { envelopeService.countTotalAmountAndCount(id) }
        ) { category, (_, totalAmounts, totalCounts) ->
            LedgerDetailResponse(
                ledger = LedgerModel.from(ledger),
                category = CategoryWithCustomModel.of(category, categoryAssignment.customCategory),
                totalAmounts = totalAmounts,
                totalCounts = totalCounts
            )
        }
    }

    suspend fun search(
        user: AuthUser,
        request: SearchLedgerRequest,
        pageRequest: SusuPageRequest,
    ): Page<SearchLedgerResponse> {
        val searchSpec = SearchLedgerSpec(
            uid = user.uid,
            title = request.title,
            categoryIds = request.categoryIds,
            fromStartAt = request.fromStartAt,

            // TODO : request.toEndAt은 안드 대응요, 안드에서 toStartAt으로 조건 변경해야함.
            toStartAt = request.toStartAt ?: request.toEndAt
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
        val ledgers = ledgerService.findAllByUidAndIdIn(user.uid, ids.toList())

        ledgers.forEach { leder ->
            txTemplate.writer.coExecuteOrNull(Dispatchers.IO + MDCContext()) {
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

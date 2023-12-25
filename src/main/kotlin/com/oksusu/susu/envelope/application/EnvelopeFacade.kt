package com.oksusu.susu.envelope.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryAssignmentService
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.category.domain.CategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.model.request.CreateEnvelopeRequest
import com.oksusu.susu.envelope.model.response.CreateEnvelopeResponse
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.friend.application.FriendService
import org.springframework.stereotype.Service

@Service
class EnvelopeFacade(
    private val envelopeService: EnvelopeService,
    private val friendService: FriendService,
    private val categoryService: CategoryService,
    private val categoryAssignmentService: CategoryAssignmentService,
    private val txTemplates: TransactionTemplates,
) {
    suspend fun create(user: AuthUser, request: CreateEnvelopeRequest): CreateEnvelopeResponse {
        val friend = friendService.findByIdAndUidOrThrow(request.friendId, user.id)
        val category = categoryService.getCategory(request.category.id)

        /** 기타 항목인 경우에만 커스텀 카테고리를 생성한다. */
        val customCategory = when (category.id == 5L) {
            true -> request.category.customCategory
            else -> null
        }

        val createdEnvelope = txTemplates.writer.executeWithContext {
            val createdEnvelope = Envelope(
                uid = user.id,
                type = request.type,
                friendId = friend.id,
                amount = request.amount,
                gift = request.gift,
                memo = request.memo,
                hasVisited = request.hasVisited,
                handedOverAt = request.handedOverAt
            ).run { envelopeService.saveSync(this) }

            CategoryAssignment(
                targetId = createdEnvelope.id,
                targetType = CategoryAssignmentType.ENVELOPE,
                categoryId = category.id,
                customCategory = customCategory
            ).run { categoryAssignmentService.saveSync(this) }

            createdEnvelope
        } ?: throw FailToCreateException(ErrorCode.FAIL_TO_CREATE_ENVELOPE_ERROR)

        return CreateEnvelopeResponse.from(createdEnvelope)
    }
}

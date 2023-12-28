package com.oksusu.susu.envelope.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryAssignmentService
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.category.domain.CategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.category.model.CategoryWithCustomModel
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.model.EnvelopeModel
import com.oksusu.susu.envelope.model.request.CreateAndUpdateEnvelopeRequest
import com.oksusu.susu.envelope.model.response.CreateAndUpdateEnvelopeResponse
import com.oksusu.susu.envelope.model.response.EnvelopeDetailResponse
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.friend.application.FriendService
import com.oksusu.susu.friend.application.RelationshipService
import com.oksusu.susu.friend.model.FriendModel
import org.springframework.stereotype.Service

@Service
class EnvelopeFacade(
    private val envelopeService: EnvelopeService,
    private val friendService: FriendService,
    private val relationshipService: RelationshipService,
    private val categoryService: CategoryService,
    private val categoryAssignmentService: CategoryAssignmentService,
    private val txTemplates: TransactionTemplates,
) {
    suspend fun create(user: AuthUser, request: CreateAndUpdateEnvelopeRequest): CreateAndUpdateEnvelopeResponse {
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

        return CreateAndUpdateEnvelopeResponse.from(createdEnvelope)
    }

    suspend fun update(
        user: AuthUser,
        id: Long,
        request: CreateAndUpdateEnvelopeRequest,
    ): CreateAndUpdateEnvelopeResponse {
        val (envelope, friend, _, categoryAssignment) = envelopeService.getDetail(id, user.id)
        val category = categoryService.getCategory(request.category.id)

        /** 기타 항목인 경우에만 커스텀 카테고리를 생성한다. */
        val customCategory = when (category.id == 5L) {
            true -> request.category.customCategory
            else -> null
        }

        val updatedEnvelope = txTemplates.writer.executeWithContext {
            val updatedEnvelope = envelope.apply {
                this.type = request.type
                this.friendId = friend.id
                this.amount = request.amount
                this.gift = request.gift
                this.memo = request.memo
                this.hasVisited = request.hasVisited
                this.handedOverAt = request.handedOverAt
            }.run { envelopeService.saveSync(this) }

            categoryAssignment.apply {
                this.categoryId = category.id
                this.customCategory = customCategory
            }.run { categoryAssignmentService.saveSync(this) }

            updatedEnvelope
        } ?: throw FailToCreateException(ErrorCode.FAIL_TO_CREATE_ENVELOPE_ERROR)

        return CreateAndUpdateEnvelopeResponse.from(updatedEnvelope)
    }

    suspend fun getDetail(user: AuthUser, id: Long): EnvelopeDetailResponse {
        val envelopeDetail = envelopeService.getDetail(id, user.id)
        val category = categoryService.getCategory(envelopeDetail.categoryAssignment.categoryId)
        val relation = relationshipService.getRelationship(envelopeDetail.friendRelationship.relationshipId)

        return EnvelopeDetailResponse(
            envelope = EnvelopeModel.from(envelopeDetail.envelope),
            category = CategoryWithCustomModel.of(category, envelopeDetail.categoryAssignment),
            relation = relation,
            friend = FriendModel.from(envelopeDetail.friend)
        )
    }

    suspend fun delete(user: AuthUser, id: Long) {
        val envelope = envelopeService.findByIdOrThrow(id, user.id)

        txTemplates.writer.executeWithContext {
            /** 봉투 삭제 */
            envelopeService.deleteSync(envelope)

            /** 카테고리 삭제 */
            categoryAssignmentService.deleteByTargetIdAndTargetTypeSync(
                targetId = envelope.id,
                targetType = CategoryAssignmentType.ENVELOPE
            )
        }
    }
}

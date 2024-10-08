package com.oksusu.susu.api.envelope.application

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.category.application.CategoryAssignmentService
import com.oksusu.susu.api.category.application.CategoryService
import com.oksusu.susu.api.category.model.CategoryWithCustomModel
import com.oksusu.susu.api.category.model.request.CreateCategoryAssignmentRequest
import com.oksusu.susu.api.common.dto.SusuPageRequest
import com.oksusu.susu.api.envelope.model.EnvelopeModel
import com.oksusu.susu.api.envelope.model.request.CreateAndUpdateEnvelopeRequest
import com.oksusu.susu.api.envelope.model.request.SearchEnvelopeRequest
import com.oksusu.susu.api.envelope.model.request.SearchFriendStatisticsRequest
import com.oksusu.susu.api.envelope.model.response.CreateAndUpdateEnvelopeResponse
import com.oksusu.susu.api.envelope.model.response.EnvelopeDetailResponse
import com.oksusu.susu.api.envelope.model.response.GetFriendStatisticsResponse
import com.oksusu.susu.api.envelope.model.response.SearchEnvelopeResponse
import com.oksusu.susu.api.event.model.CreateEnvelopeEvent
import com.oksusu.susu.api.event.model.DeleteEnvelopeEvent
import com.oksusu.susu.api.event.model.UpdateEnvelopeEvent
import com.oksusu.susu.api.friend.application.FriendRelationshipService
import com.oksusu.susu.api.friend.application.FriendService
import com.oksusu.susu.api.friend.application.RelationshipService
import com.oksusu.susu.api.friend.model.FriendModel
import com.oksusu.susu.api.friend.model.FriendRelationshipModel
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.parZipWithMDC
import com.oksusu.susu.domain.category.domain.CategoryAssignment
import com.oksusu.susu.domain.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.domain.common.extension.coExecute
import com.oksusu.susu.domain.common.extension.coExecuteOrNull
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.envelope.infrastructure.model.SearchEnvelopeSpec
import com.oksusu.susu.domain.envelope.infrastructure.model.SearchFriendStatisticsSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class EnvelopeFacade(
    private val envelopeService: EnvelopeService,
    private val friendService: FriendService,
    private val relationshipService: RelationshipService,
    private val categoryService: CategoryService,
    private val categoryAssignmentService: CategoryAssignmentService,
    private val ledgerService: LedgerService,
    private val friendRelationshipService: FriendRelationshipService,
    private val txTemplates: TransactionTemplates,
    private val publisher: ApplicationEventPublisher,
    private val envelopeValidateService: EnvelopeValidateService,
) {
    suspend fun create(user: AuthUser, request: CreateAndUpdateEnvelopeRequest): CreateAndUpdateEnvelopeResponse {
        envelopeValidateService.validateEnvelopeRequest(request)

        return parZipWithMDC(
            { friendService.findByIdAndUidOrThrow(request.friendId, user.uid) },
            { friendRelationshipService.findByFriendIdOrThrow(request.friendId) },
            {
                when (request.ledgerId != null) {
                    true -> ledgerService.findByIdAndUidOrNull(request.ledgerId, user.uid)
                    false -> null
                }
            },
            {
                when (request.ledgerId != null) {
                    true -> categoryAssignmentService.findByIdAndTypeOrNull(
                        request.ledgerId,
                        CategoryAssignmentType.LEDGER
                    )

                    false -> null
                }
            }
        ) { friend, friendRelationship, ledger, ledgerCategory ->
            val categoryAssignmentRequest = if (ledgerCategory == null) {
                when (request.category == null) {
                    true -> CreateCategoryAssignmentRequest(5)
                    false -> request.category
                }
            } else {
                CreateCategoryAssignmentRequest(ledgerCategory.categoryId, ledgerCategory.customCategory)
            }

            val category = categoryService.getCategory(categoryAssignmentRequest.id)

            /** 기타 항목인 경우에만 커스텀 카테고리를 생성한다. */
            val customCategory = when (category.isMiscCategory()) {
                true -> categoryAssignmentRequest.customCategory
                false -> null
            }

            val createdEnvelope = txTemplates.writer.coExecute(Dispatchers.IO + MDCContext()) {
                val createdEnvelope = Envelope(
                    uid = user.uid,
                    type = request.type,
                    friendId = friend.id,
                    ledgerId = request.ledgerId,
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

                publisher.publishEvent(CreateEnvelopeEvent(createdEnvelope, ledger, user))

                createdEnvelope
            }
            val relationship = relationshipService.getRelationship(friendRelationship.relationshipId)

            CreateAndUpdateEnvelopeResponse.of(
                envelope = createdEnvelope,
                friend = friend,
                friendRelationship = friendRelationship,
                relationship = relationship,
                category = CategoryWithCustomModel.of(category, customCategory)
            )
        }
    }

    suspend fun update(
        user: AuthUser,
        id: Long,
        request: CreateAndUpdateEnvelopeRequest,
    ): CreateAndUpdateEnvelopeResponse {
        envelopeValidateService.validateEnvelopeRequest(request)

        return parZipWithMDC(
            {
                envelopeService.getDetail(id, user.uid)
            },
            {
                when (request.ledgerId != null) {
                    true -> categoryAssignmentService.findByIdAndTypeOrNull(
                        request.ledgerId,
                        CategoryAssignmentType.LEDGER
                    )

                    false -> null
                }
            }
        ) { (envelope, friend, friendRelationship, categoryAssignment), ledgerCategory ->
            val categoryAssignmentRequest = if (ledgerCategory == null) {
                when (request.category == null) {
                    true -> CreateCategoryAssignmentRequest(5)
                    false -> request.category
                }
            } else {
                CreateCategoryAssignmentRequest(ledgerCategory.categoryId, ledgerCategory.customCategory)
            }

            val category = categoryService.getCategory(categoryAssignmentRequest.id)

            /** 기타 항목인 경우에만 커스텀 카테고리를 생성한다. */
            val customCategory = when (category.isMiscCategory()) {
                true -> categoryAssignmentRequest.customCategory
                else -> null
            }

            val updatedEnvelope = txTemplates.writer.coExecute(Dispatchers.IO + MDCContext()) {
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

                publisher.publishEvent(UpdateEnvelopeEvent(updatedEnvelope, user))

                updatedEnvelope
            }
            val relationship = relationshipService.getRelationship(friendRelationship.relationshipId)

            CreateAndUpdateEnvelopeResponse.of(
                envelope = updatedEnvelope,
                friend = friend,
                friendRelationship = friendRelationship,
                relationship = relationship,
                category = CategoryWithCustomModel.of(category, customCategory)
            )
        }
    }

    suspend fun getDetail(user: AuthUser, id: Long): EnvelopeDetailResponse {
        val envelopeDetail = envelopeService.getDetail(id, user.uid)
        val category = categoryService.getCategory(envelopeDetail.categoryAssignment.categoryId)
        val relation = relationshipService.getRelationship(envelopeDetail.friendRelationship.relationshipId)

        return EnvelopeDetailResponse(
            envelope = EnvelopeModel.from(envelopeDetail.envelope),
            category = CategoryWithCustomModel.of(category, envelopeDetail.categoryAssignment),
            relationship = relation,
            friendRelationship = FriendRelationshipModel.from(envelopeDetail.friendRelationship),
            friend = FriendModel.from(envelopeDetail.friend)
        )
    }

    suspend fun delete(user: AuthUser, id: Long) {
        val envelope = envelopeService.findByIdOrThrow(id, user.uid)

        txTemplates.writer.coExecuteOrNull(Dispatchers.IO + MDCContext()) {
            /** 봉투 삭제 */
            envelopeService.deleteSync(envelope)

            /** 카테고리 삭제 */
            categoryAssignmentService.deleteByTargetIdAndTargetTypeSync(
                targetId = envelope.id,
                targetType = CategoryAssignmentType.ENVELOPE
            )

            DeleteEnvelopeEvent(envelope, user)
                .run { publisher.publishEvent(this) }
        }
    }

    suspend fun search(
        user: AuthUser,
        request: SearchEnvelopeRequest,
        pageRequest: SusuPageRequest,
    ): Page<SearchEnvelopeResponse> {
        return search(user.uid, request, pageRequest)
    }

    suspend fun search(
        uid: Long,
        request: SearchEnvelopeRequest,
        pageRequest: SusuPageRequest,
    ): Page<SearchEnvelopeResponse> {
        val response = envelopeService.search(resolveSearchSpec(uid, request), pageRequest.toDefault())

        return response.map { (envelope, friend, friendRelationship, categoryAssignments) ->
            val category = categoryAssignments?.let { categoryAssignment ->
                val category = categoryService.getCategory(categoryAssignment.categoryId)
                CategoryWithCustomModel.of(category, categoryAssignment)
            }
            val relationship = friendRelationship?.relationshipId
                ?.let { relationshipId -> relationshipService.getRelationship(relationshipId) }

            SearchEnvelopeResponse.of(
                envelope = envelope,
                category = category,
                friend = friend,
                relationship = relationship,
                friendRelationship = friendRelationship,
                include = request.include
            )
        }
    }

    private fun resolveSearchSpec(uid: Long, request: SearchEnvelopeRequest): SearchEnvelopeSpec {
        return SearchEnvelopeSpec(
            uid = uid,
            friendId = request.friendIds,
            friendName = request.friendName,
            ledgerId = request.ledgerId,
            types = request.types,
            fromAmount = request.fromAmount,
            toAmount = request.toAmount
        )
    }

    suspend fun searchFriendStatistics(
        user: AuthUser,
        request: SearchFriendStatisticsRequest,
        pageRequest: SusuPageRequest,
    ): Page<GetFriendStatisticsResponse> {
        val pageable = pageRequest.toDefault()
        val searchSpec = SearchFriendStatisticsSpec(
            uid = user.uid,
            friendIds = request.friendIds,
            fromTotalAmounts = request.fromTotalAmounts,
            toTotalAmounts = request.toTotalAmounts
        )

        val friendStatistics = envelopeService.findFriendStatistics(searchSpec, pageable)

        val friendIds = friendStatistics.map { statistics -> statistics.friendId }.toList()
        val friends = friendService.findAllByIdIn(friendIds).associateBy { friend -> friend.id }

        return friendStatistics.map { statistics ->
            val friend = friends[statistics.friendId] ?: throw NotFoundException(ErrorCode.NOT_FOUND_FRIEND_ERROR)

            GetFriendStatisticsResponse(
                friend = FriendModel.from(friend),
                totalAmounts = statistics.sentAmounts + statistics.receivedAmounts,
                sentAmounts = statistics.sentAmounts,
                receivedAmounts = statistics.receivedAmounts
            )
        }
    }
}

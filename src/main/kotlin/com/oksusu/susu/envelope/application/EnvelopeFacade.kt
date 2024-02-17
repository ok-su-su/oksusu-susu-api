package com.oksusu.susu.envelope.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryAssignmentService
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.category.domain.CategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.category.model.CategoryWithCustomModel
import com.oksusu.susu.category.model.request.CreateCategoryAssignmentRequest
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.model.SearchEnvelopeSpec
import com.oksusu.susu.envelope.infrastructure.model.SearchFriendStatisticsSpec
import com.oksusu.susu.envelope.model.EnvelopeModel
import com.oksusu.susu.envelope.model.request.CreateAndUpdateEnvelopeRequest
import com.oksusu.susu.envelope.model.request.SearchEnvelopeRequest
import com.oksusu.susu.envelope.model.request.SearchFriendStatisticsRequest
import com.oksusu.susu.envelope.model.response.CreateAndUpdateEnvelopeResponse
import com.oksusu.susu.envelope.model.response.EnvelopeDetailResponse
import com.oksusu.susu.envelope.model.response.GetFriendStatisticsResponse
import com.oksusu.susu.envelope.model.response.SearchEnvelopeResponse
import com.oksusu.susu.event.model.DeleteEnvelopeEvent
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.extension.coExecuteOrNull
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.friend.application.FriendService
import com.oksusu.susu.friend.application.RelationshipService
import com.oksusu.susu.friend.model.FriendModel
import com.oksusu.susu.friend.model.FriendRelationshipModel
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
) {
    suspend fun create(user: AuthUser, request: CreateAndUpdateEnvelopeRequest): CreateAndUpdateEnvelopeResponse {
        return parZip(
            { friendService.findByIdAndUidOrThrow(request.friendId, user.uid) },
            { friendRelationshipService.findByFriendIdOrThrow(request.friendId) },
            {
                when (request.ledgerId != null) {
                    true -> ledgerService.findByIdAndUidOrNull(request.ledgerId, user.uid)
                    false -> null
                }
            }
        ) { friend, friendRelationship, ledger ->
            val categoryAssignmentRequest = when (request.category == null) {
                true -> CreateCategoryAssignmentRequest(5)
                false -> request.category
            }

            val category = categoryService.getCategory(categoryAssignmentRequest.id)

            /** 기타 항목인 경우에만 커스텀 카테고리를 생성한다. */
            val customCategory = when (category.isMiscCategory()) {
                true -> categoryAssignmentRequest.customCategory
                false -> null
            }

            val createdEnvelope = txTemplates.writer.coExecute {
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

                // TODO 바꿔야함..
                ledger?.let {
                    if (createdEnvelope.type == EnvelopeType.RECEIVED) {
                        it.totalReceivedAmounts = it.totalReceivedAmounts + createdEnvelope.amount
                    }

                    if (createdEnvelope.type == EnvelopeType.SENT) {
                        it.totalSentAmounts = it.totalSentAmounts + createdEnvelope.amount
                    }

                    ledgerService.saveSync(it)
                }

                createdEnvelope
            }
            val relationship = relationshipService.getRelationship(friendRelationship.relationshipId)

            CreateAndUpdateEnvelopeResponse.of(
                envelope = createdEnvelope,
                friend = friend,
                friendRelationship = friendRelationship,
                relationship = relationship
            )
        }
    }

    suspend fun update(
        user: AuthUser,
        id: Long,
        request: CreateAndUpdateEnvelopeRequest,
    ): CreateAndUpdateEnvelopeResponse {
        val categoryAssignmentRequest = when (request.category == null) {
            true -> CreateCategoryAssignmentRequest(5)
            false -> request.category
        }

        val (envelope, friend, friendRelationship, categoryAssignment) = envelopeService.getDetail(id, user.uid)
        val category = categoryService.getCategory(categoryAssignmentRequest.id)

        /** 기타 항목인 경우에만 커스텀 카테고리를 생성한다. */
        val customCategory = when (category.isMiscCategory()) {
            true -> categoryAssignmentRequest.customCategory
            else -> null
        }

        val updatedEnvelope = txTemplates.writer.coExecute {
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
        }

        val relationship = relationshipService.getRelationship(friendRelationship.relationshipId)

        return CreateAndUpdateEnvelopeResponse.of(
            envelope = updatedEnvelope,
            friend = friend,
            friendRelationship = friendRelationship,
            relationship = relationship
        )
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

        txTemplates.writer.coExecuteOrNull {
            /** 봉투 삭제 */
            envelopeService.deleteSync(envelope)

            /** 카테고리 삭제 */
            categoryAssignmentService.deleteByTargetIdAndTargetTypeSync(
                targetId = envelope.id,
                targetType = CategoryAssignmentType.ENVELOPE
            )

            DeleteEnvelopeEvent(
                envelopeId = envelope.id,
                uid = user.uid,
                friendId = envelope.friendId
            ).run { publisher.publishEvent(this) }
        }
    }

    suspend fun search(
        user: AuthUser,
        request: SearchEnvelopeRequest,
        pageRequest: SusuPageRequest,
    ): Page<SearchEnvelopeResponse> {
        val searchSpec = SearchEnvelopeSpec(
            uid = user.uid,
            friendId = request.friendIds,
            ledgerId = request.ledgerId,
            types = request.types,
            fromAmount = request.fromAmount,
            toAmount = request.toAmount
        )
        val pageable = pageRequest.toDefault()

        val response = envelopeService.search(searchSpec, pageable)
        val friendIds = response.content.map { envelope -> envelope.friendId }
        val envelopeIds = response.content.map { envelope -> envelope.id }

        return parZip(
            {
                when (request.includeFriend) {
                    true -> friendService.findAllByIdIn(friendIds)
                    false -> emptyList()
                }.associateBy { friend -> friend.id }
            },
            {
                when (request.includeRelationship || request.includeFriendRelationship) {
                    true -> friendRelationshipService.findAllByFriendIds(friendIds)
                    false -> emptyList()
                }.associateBy { friendRelationShip -> friendRelationShip.friendId }
            },
            {
                when (request.includeCategory) {
                    true -> categoryAssignmentService.findAllByTypeAndIdIn(CategoryAssignmentType.ENVELOPE, envelopeIds)
                    false -> emptyList()
                }.associateBy { categoryAssignment -> categoryAssignment.targetId }
            }
        ) { friends, friendRelationships, categoryAssignments ->
            response.map { envelope ->
                val category = categoryAssignments[envelope.id]?.let { categoryAssignment ->
                    val category = categoryService.getCategory(categoryAssignment.categoryId)
                    CategoryWithCustomModel.of(category, categoryAssignment)
                }
                val relationship = friendRelationships[envelope.friendId]?.let { friendRelationship ->
                    relationshipService.getRelationship(friendRelationship.relationshipId)
                }
                val friend = friends[envelope.friendId]?.let { friend ->
                    FriendModel.from(friend)
                }
                val friendRelationship = friendRelationships[envelope.friendId]?.let { friendRelationship ->
                    FriendRelationshipModel.from(friendRelationship)
                }

                SearchEnvelopeResponse(
                    envelope = EnvelopeModel.from(envelope),
                    category = category,
                    friend = friend,
                    relationship = relationship,
                    friendRelationship = friendRelationship
                )
            }
        }
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

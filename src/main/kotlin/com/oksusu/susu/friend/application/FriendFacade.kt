package com.oksusu.susu.friend.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryAssignmentService
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.exception.AlreadyException
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.friend.domain.Friend
import com.oksusu.susu.friend.domain.FriendRelationship
import com.oksusu.susu.friend.infrastructure.model.SearchFriendSpec
import com.oksusu.susu.friend.model.request.CreateFriendRequest
import com.oksusu.susu.friend.model.request.SearchFriendRequest
import com.oksusu.susu.friend.model.response.CreateFriendResponse
import com.oksusu.susu.friend.model.response.RecentEnvelopeModel
import com.oksusu.susu.friend.model.response.SearchFriendResponse
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class FriendFacade(
    private val friendService: FriendService,
    private val friendRelationshipService: FriendRelationshipService,
    private val relationshipService: RelationshipService,
    private val txTemplates: TransactionTemplates,
    private val envelopeService: EnvelopeService,
    private val categoryAssignmentService: CategoryAssignmentService,
    private val categoryService: CategoryService,
) {
    suspend fun search(
        user: AuthUser,
        searchRequest: SearchFriendRequest,
        pageRequest: SusuPageRequest,
    ): Page<SearchFriendResponse> {
        val searchResponse = friendService.search(
            spec = SearchFriendSpec(
                uid = user.id,
                name = searchRequest.name,
                phoneNumber = searchRequest.phoneNumber
            ),
            pageable = pageRequest.toDefault()
        )

        val friendIds = searchResponse.map { it.friend.id }.toSet()

        val envelopes = envelopeService.findLatestFriendEnvelopes(friendIds)

        val envelopeMap = envelopes.associateBy { envelope -> envelope.friendId }

        val categoryAssignments = categoryAssignmentService.findAllByTypeAndIdIn(
            targetType = CategoryAssignmentType.ENVELOPE,
            targetIds = envelopes.map { envelope -> envelope.id }
        ).associateBy { categoryAssignment -> categoryAssignment.targetId }

        return searchResponse.map { (friend, friendRelationship) ->
            val relationship = relationshipService.getRelationship(friendRelationship.relationshipId)

            val envelope = envelopeMap[friend.id]
            val categoryAssignment = envelope?.let { target -> categoryAssignments[target.id] }
            val category = categoryAssignment?.categoryId?.let { target -> categoryService.getCategory(target) }

            val recentEnvelopeModel = if (envelope != null && category != null && categoryAssignment != null) {
                RecentEnvelopeModel(
                    category = categoryAssignment.customCategory ?: category.name,
                    handedOverAt = envelope.handedOverAt
                )
            } else {
                null
            }

            SearchFriendResponse.of(
                friend = friend,
                relationship = relationship,
                friendRelationship = friendRelationship,
                recentEnvelope = recentEnvelopeModel
            )
        }
    }

    suspend fun create(user: AuthUser, request: CreateFriendRequest): CreateFriendResponse {
        if (request.phoneNumber != null) {
            if (friendService.existsByPhoneNumber(user.id, request.phoneNumber)) {
                throw AlreadyException(ErrorCode.ALREADY_REGISTERED_FRIEND_PHONE_NUMBER_ERROR)
            }
        }

        val relationship = relationshipService.getRelationship(request.relationshipId)
        val customRelation = when (relationship.id == 5L) {
            true -> request.customRelation
            false -> null
        }

        val createdFriend = txTemplates.writer.coExecute {
            val createdFriend = Friend(
                uid = user.id,
                name = request.name,
                phoneNumber = request.phoneNumber
            ).run { friendService.saveSync(this) }

            FriendRelationship(
                friendId = createdFriend.id,
                relationshipId = relationship.id,
                customRelation = customRelation
            ).run { friendRelationshipService.saveSync(this) }

            createdFriend
        }

        return CreateFriendResponse(createdFriend.id)
    }
}

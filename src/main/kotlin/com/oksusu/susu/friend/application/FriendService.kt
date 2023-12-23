package com.oksusu.susu.friend.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.AlreadyException
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.friend.domain.Friend
import com.oksusu.susu.friend.domain.FriendRelationship
import com.oksusu.susu.friend.infrastructure.FriendRepository
import com.oksusu.susu.friend.infrastructure.model.FriendAndFriendRelationshipModel
import com.oksusu.susu.friend.infrastructure.model.SearchFriendRequestModel
import com.oksusu.susu.friend.model.request.CreateFriendRequest
import com.oksusu.susu.friend.model.request.SearchFriendRequest
import com.oksusu.susu.friend.model.response.CreateFriendResponse
import com.oksusu.susu.friend.model.response.SearchFriendResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class FriendService(
    private val friendRepository: FriendRepository,
    private val friendRelationshipService: FriendRelationshipService,
    private val relationshipService: RelationshipService,
    private val txTemplates: TransactionTemplates,
) {
    suspend fun search(
        user: AuthUser,
        searchRequest: SearchFriendRequest,
        pageRequest: SusuPageRequest,
    ): Page<SearchFriendResponse> {
        val searchResponse = search(
            searchRequest = SearchFriendRequestModel(
                uid = user.id,
                name = searchRequest.name,
                phoneNumber = searchRequest.phoneNumber
            ),
            pageable = pageRequest.toDefault()
        )

        return searchResponse.map { (friend, friendRelationship) ->
            val relationship = relationshipService.getRelationship(friendRelationship.relationshipId)

            SearchFriendResponse.of(
                friend = friend,
                relationship = relationship,
                friendRelationship = friendRelationship
            )
        }
    }

    suspend fun search(
        searchRequest: SearchFriendRequestModel,
        pageable: Pageable,
    ): Page<FriendAndFriendRelationshipModel> {
        return withContext(Dispatchers.IO) { friendRepository.search(searchRequest, pageable) }
    }

    suspend fun findByIdAndUidOrThrow(id: Long, uid: Long): Friend {
        return findByIdAndUidOrNull(id, uid) ?: throw NotFoundException(ErrorCode.NOT_FOUND_FRIEND_ERROR)
    }

    suspend fun findByIdAndUidOrNull(id: Long, uid: Long): Friend? {
        return withContext(Dispatchers.IO) { friendRepository.findByIdAndUid(id, uid) }
    }

    suspend fun create(user: AuthUser, request: CreateFriendRequest): CreateFriendResponse {
        if (request.phoneNumber != null) {
            if (existsByPhoneNumber(user.id, request.phoneNumber)) {
                throw AlreadyException(ErrorCode.ALREADY_REGISTERED_FRIEND_PHONE_NUMBER_ERROR)
            }
        }

        val relationship = relationshipService.getRelationship(request.relationshipId)
        val customRelation = when (relationship.id == 5L) {
            true -> request.customRelation
            false -> null
        }

        val createdFriend = txTemplates.writer.executeWithContext {
            val createdFriend = Friend(
                uid = user.id,
                name = request.name,
                phoneNumber = request.phoneNumber
            ).run { saveSync(this) }

            FriendRelationship(
                friendId = createdFriend.id,
                relationshipId = relationship.id,
                customRelation = customRelation
            ).run { friendRelationshipService.saveSync(this) }

            createdFriend
        } ?: throw FailToCreateException(ErrorCode.FAIL_TO_CREATE_FRIEND_ERROR)

        return CreateFriendResponse(createdFriend.id)
    }

    fun saveSync(friend: Friend): Friend {
        return friendRepository.save(friend)
    }

    suspend fun existsByPhoneNumber(uid: Long, phoneNumber: String): Boolean {
        return withContext(Dispatchers.IO) { friendRepository.existsByUidAndPhoneNumber(uid, phoneNumber) }
    }
}
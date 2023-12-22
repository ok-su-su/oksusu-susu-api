package com.oksusu.susu.friend.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.friend.infrastructure.FriendRepository
import com.oksusu.susu.friend.infrastructure.model.FriendAndFriendRelationshipModel
import com.oksusu.susu.friend.infrastructure.model.SearchFriendRequestModel
import com.oksusu.susu.friend.model.request.SearchFriendRequest
import com.oksusu.susu.friend.model.response.SearchFriendResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class FriendService(
    private val friendRepository: FriendRepository,
    private val relationshipService: RelationshipService,
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
}

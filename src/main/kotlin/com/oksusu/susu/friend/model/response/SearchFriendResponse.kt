package com.oksusu.susu.friend.model.response

import com.oksusu.susu.friend.domain.Friend
import com.oksusu.susu.friend.domain.FriendRelationship
import com.oksusu.susu.friend.model.FriendModel
import com.oksusu.susu.friend.model.RelationshipInfoModel
import com.oksusu.susu.friend.model.RelationshipModel

data class SearchFriendResponse(
    val friend: FriendModel,
    val relationship: RelationshipInfoModel,
) {
    companion object {
        fun of(
            friend: Friend,
            relationship: RelationshipModel,
            friendRelationship: FriendRelationship,
        ): SearchFriendResponse {
            return SearchFriendResponse(
                friend = FriendModel.from(friend),
                relationship = RelationshipInfoModel.of(relationship, friendRelationship)
            )
        }
    }
}

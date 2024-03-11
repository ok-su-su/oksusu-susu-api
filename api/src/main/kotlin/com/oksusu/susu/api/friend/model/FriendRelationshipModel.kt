package com.oksusu.susu.api.friend.model

import com.oksusu.susu.api.friend.domain.FriendRelationship

data class FriendRelationshipModel(
    val id: Long,
    val friendId: Long,
    val relationshipId: Long,
    val customRelation: String?,
) {
    companion object {
        fun from(friendRelationship: FriendRelationship): FriendRelationshipModel {
            return FriendRelationshipModel(
                id = friendRelationship.id,
                friendId = friendRelationship.friendId,
                relationshipId = friendRelationship.relationshipId,
                customRelation = friendRelationship.customRelation
            )
        }
    }
}

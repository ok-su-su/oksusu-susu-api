package com.oksusu.susu.friend.model.response

import com.oksusu.susu.friend.domain.Friend
import com.oksusu.susu.friend.domain.FriendRelationship
import com.oksusu.susu.friend.model.FriendModel
import com.oksusu.susu.friend.model.RelationshipInfoModel
import com.oksusu.susu.friend.model.RelationshipModel
import java.time.LocalDateTime

data class SearchFriendResponse(
    val friend: FriendModel,
    val relationship: RelationshipInfoModel,
    val recentEnvelope: RecentEnvelopeModel?,
) {
    companion object {
        fun of(
            friend: Friend,
            relationship: RelationshipModel,
            friendRelationship: FriendRelationship,
            recentEnvelope: RecentEnvelopeModel?,
        ): SearchFriendResponse {
            return SearchFriendResponse(
                friend = FriendModel.from(friend),
                relationship = RelationshipInfoModel.of(relationship, friendRelationship),
                recentEnvelope = recentEnvelope
            )
        }
    }
}

data class RecentEnvelopeModel(
    val category: String,
    val handedOverAt: LocalDateTime,
)

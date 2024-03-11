package com.oksusu.susu.api.friend.model.response

import com.oksusu.susu.domain.friend.domain.Friend
import com.oksusu.susu.domain.friend.domain.FriendRelationship
import com.oksusu.susu.api.friend.model.FriendModel
import com.oksusu.susu.api.friend.model.RelationshipInfoModel
import com.oksusu.susu.api.friend.model.RelationshipModel
import java.time.LocalDateTime

data class SearchFriendResponse(
    /** 친구 */
    val friend: FriendModel,
    /** 관계 */
    val relationship: RelationshipInfoModel,
    /** 최근 생성된 봉투 정보 */
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
    /** 카테고리 */
    val category: String,
    /** 전달일 */
    val handedOverAt: LocalDateTime,
)

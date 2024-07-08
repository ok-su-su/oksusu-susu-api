package com.oksusu.susu.api.envelope.model.response

import com.oksusu.susu.api.category.model.CategoryWithCustomModel
import com.oksusu.susu.api.envelope.model.EnvelopeModel
import com.oksusu.susu.api.friend.model.FriendModel
import com.oksusu.susu.api.friend.model.FriendRelationshipModel
import com.oksusu.susu.api.friend.model.RelationshipModel
import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.friend.domain.Friend
import com.oksusu.susu.domain.friend.domain.FriendRelationship

data class CreateAndUpdateEnvelopeResponse(
    /** 봉투 */
    val envelope: EnvelopeModel,
    /** 지인 */
    val friend: FriendModel,
    /** 지인 관계 정보 */
    val friendRelationship: FriendRelationshipModel,
    /** 관계 정보 */
    val relationship: RelationshipModel,
    /** 카테고리 정보 */
    val category: CategoryWithCustomModel,
) {
    companion object {
        fun of(
            envelope: Envelope,
            friend: Friend,
            friendRelationship: FriendRelationship,
            relationship: RelationshipModel,
            category: CategoryWithCustomModel,
        ): CreateAndUpdateEnvelopeResponse {
            return CreateAndUpdateEnvelopeResponse(
                envelope = EnvelopeModel.from(envelope),
                friend = FriendModel.from(friend),
                friendRelationship = FriendRelationshipModel.from(friendRelationship),
                relationship = relationship,
                category = category
            )
        }
    }
}

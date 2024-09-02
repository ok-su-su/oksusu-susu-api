package com.oksusu.susu.api.envelope.model.response

import com.oksusu.susu.api.category.model.CategoryWithCustomModel
import com.oksusu.susu.api.envelope.model.EnvelopeModel
import com.oksusu.susu.api.envelope.model.request.SearchEnvelopeRequest.IncludeSpec
import com.oksusu.susu.api.friend.model.FriendModel
import com.oksusu.susu.api.friend.model.FriendRelationshipModel
import com.oksusu.susu.api.friend.model.RelationshipModel
import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.friend.domain.Friend
import com.oksusu.susu.domain.friend.domain.FriendRelationship

data class SearchEnvelopeResponse(
    /** 봉투 */
    val envelope: EnvelopeModel,
    /** 카테고리 */
    val category: CategoryWithCustomModel? = null,
    /** 지인 */
    val friend: FriendModel? = null,
    /** 관계 */
    val relationship: RelationshipModel? = null,
    /** 지인 관계 정보 */
    val friendRelationship: FriendRelationshipModel? = null,
) {
    companion object {
        fun of(
            envelope: Envelope,
            category: CategoryWithCustomModel?,
            friend: Friend?,
            relationship: RelationshipModel?,
            friendRelationship: FriendRelationship?,
            include: Set<IncludeSpec>?,
        ) = SearchEnvelopeResponse(
            envelope = EnvelopeModel.from(envelope),
            category = category
                ?.takeIf { include?.contains(IncludeSpec.CATEGORY) == true },
            friend = friend
                ?.let { FriendModel.from(it) }
                ?.takeIf { include?.contains(IncludeSpec.FRIEND) == true },
            relationship = relationship
                ?.takeIf { include?.contains(IncludeSpec.RELATIONSHIP) == true },
            friendRelationship = friendRelationship
                ?.let { FriendRelationshipModel.from(it) }
                ?.takeIf { include?.contains(IncludeSpec.FRIEND_RELATIONSHIP) == true }
        )
    }
}

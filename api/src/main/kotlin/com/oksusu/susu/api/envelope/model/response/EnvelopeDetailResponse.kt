package com.oksusu.susu.api.envelope.model.response

import com.oksusu.susu.api.category.model.CategoryWithCustomModel
import com.oksusu.susu.api.envelope.model.EnvelopeModel
import com.oksusu.susu.api.friend.model.FriendModel
import com.oksusu.susu.api.friend.model.FriendRelationshipModel
import com.oksusu.susu.api.friend.model.RelationshipModel

data class EnvelopeDetailResponse(
    /** 봉투 */
    val envelope: EnvelopeModel,
    /** 카테고리 */
    val category: CategoryWithCustomModel,
    /** 관계 */
    val relationship: RelationshipModel,
    /** 친구 관계 */
    val friendRelationship: FriendRelationshipModel,
    /** 지인 */
    val friend: FriendModel,
)

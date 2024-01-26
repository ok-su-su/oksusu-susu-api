package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.category.model.CategoryWithCustomModel
import com.oksusu.susu.envelope.model.EnvelopeModel
import com.oksusu.susu.friend.model.FriendModel
import com.oksusu.susu.friend.model.RelationshipModel

data class EnvelopeDetailResponse(
    /** 봉투 */
    val envelope: EnvelopeModel,
    /** 카테고리 */
    val category: CategoryWithCustomModel,
    /** 관계 */
    val relation: RelationshipModel,
    /** 지인 */
    val friend: FriendModel,
)

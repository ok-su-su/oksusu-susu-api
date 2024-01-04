package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.category.model.CategoryWithCustomModel
import com.oksusu.susu.envelope.model.EnvelopeModel
import com.oksusu.susu.friend.model.FriendModel
import com.oksusu.susu.friend.model.RelationshipModel

data class EnvelopeDetailResponse(
    val envelope: EnvelopeModel,
    val category: CategoryWithCustomModel,
    val relation: RelationshipModel,
    val friend: FriendModel,
)

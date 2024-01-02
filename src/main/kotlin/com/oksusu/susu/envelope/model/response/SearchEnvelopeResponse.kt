package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.category.model.CategoryWithCustomModel
import com.oksusu.susu.envelope.model.EnvelopeModel
import com.oksusu.susu.friend.model.FriendModel
import com.oksusu.susu.friend.model.RelationshipModel

data class SearchEnvelopeResponse(
    val envelope: EnvelopeModel,
    val category: CategoryWithCustomModel? = null,
    val friend: FriendModel? = null,
    val relation: RelationshipModel? = null,
)

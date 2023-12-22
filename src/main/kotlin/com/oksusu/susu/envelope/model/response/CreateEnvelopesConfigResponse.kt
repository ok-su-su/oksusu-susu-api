package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.friend.model.RelationshipModel

data class CreateEnvelopesConfigResponse(
    val categories: List<CategoryModel>,
    val relationships: List<RelationshipModel>,
)

package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.friend.model.RelationshipModel

data class CreateEnvelopesConfigResponse(
    /** 카테고리 정보 */
    val categories: List<CategoryModel>,
    /** 관계 정보 */
    val relationships: List<RelationshipModel>,
)

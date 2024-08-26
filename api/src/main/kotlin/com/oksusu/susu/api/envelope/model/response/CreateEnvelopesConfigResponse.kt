package com.oksusu.susu.api.envelope.model.response

import com.oksusu.susu.api.category.model.CategoryModel
import com.oksusu.susu.api.friend.model.RelationshipModel

data class CreateEnvelopesConfigResponse(
    /** 카테고리 정보, 활성화된 정보만 제공 */
    val categories: List<CategoryModel>,
    /** 관계 정보 */
    val relationships: List<RelationshipModel>,
)

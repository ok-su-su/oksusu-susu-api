package com.oksusu.susu.api.friend.model

import com.oksusu.susu.api.friend.domain.Relationship

data class RelationshipModel(
    /** 관계 id */
    val id: Long,
    /** 관계 */
    val relation: String,
    /** 설명 */
    val description: String?,
) {
    companion object {
        fun from(relationship: Relationship): RelationshipModel {
            return RelationshipModel(
                id = relationship.id,
                relation = relationship.relation,
                description = relationship.description
            )
        }
    }
}

package com.oksusu.susu.friend.model

import com.oksusu.susu.friend.domain.Relationship

data class RelationshipModel(
    val id: Long,
    val relation: String,
    val description: String,
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

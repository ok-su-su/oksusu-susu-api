package com.oksusu.susu.friend.model

import com.oksusu.susu.friend.domain.FriendRelationship

data class RelationshipInfoModel(
    val id: Long,
    val relation: String,
    val customRelation: String?,
    val description: String?,
) {
    companion object {
        fun of(relationship: RelationshipModel, friendRelationship: FriendRelationship): RelationshipInfoModel {
            return RelationshipInfoModel(
                id = relationship.id,
                relation = relationship.relation,
                customRelation = friendRelationship.customRelation,
                description = relationship.description
            )
        }
    }
}

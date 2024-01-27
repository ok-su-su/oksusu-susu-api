package com.oksusu.susu.friend.model

import com.oksusu.susu.friend.domain.FriendRelationship

data class RelationshipInfoModel(
    /** 관계 id */
    val id: Long,
    /** 관계 */
    val relation: String,
    /** 커스컴 관계 */
    val customRelation: String?,
    /** 상세 설명 */
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

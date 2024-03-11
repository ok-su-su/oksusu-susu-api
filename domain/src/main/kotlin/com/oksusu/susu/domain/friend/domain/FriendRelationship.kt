package com.oksusu.susu.domain.friend.domain

import com.oksusu.susu.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/** 지인 관계 */
@Entity
@Table(name = "friend_relationship")
data class FriendRelationship(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 지인 id */
    @Column(name = "friend_id")
    val friendId: Long = -1L,

    /** 관계 id */
    @Column(name = "relationship_id")
    var relationshipId: Long = -1L,

    /** 기타 항목인 경우, 별도 입력을 위한 컬럼 */
    @Column(name = "custom_relation")
    var customRelation: String? = null,
) : BaseEntity()

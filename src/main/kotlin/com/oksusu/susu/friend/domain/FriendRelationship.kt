package com.oksusu.susu.friend.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "friend_relationship")
data class FriendRelationship(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(name = "friend_id")
    val friendId: Long = -1L,

    @Column(name = "relationship_id")
    val relationshipId: Long = -1L,

    @Column(name = "custom_relation")
    val customRelation: String? = null,
) : BaseEntity()

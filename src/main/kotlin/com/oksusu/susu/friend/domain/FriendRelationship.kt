package com.oksusu.susu.friend.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "friend_relationship")
@IdClass(FriendRelationshipPk::class)
data class FriendRelationship(
    @Id
    @Column(name = "friend_id")
    val friendId: Long = -1L,

    @Id
    @Column(name = "relationship_id")
    val relationshipId: Long = -1L,

    @Column(name = "custom_relation")
    val customRelation: String? = null,
) : BaseEntity()

data class FriendRelationshipPk(
    @Column(name = "friend_id")
    val friendId: Long = -1L,

    @Column(name = "relationship_id")
    val relationshipId: Long = -1L,
) : Serializable

package com.oksusu.susu.community.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "vote_option")
class VoteOption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(name = "community_id")
    val communityId: Long,

    val content: String,

    val seq: Int,
) : BaseEntity()
package com.oksusu.susu.community.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "vote_history")
class VoteHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    @Column(name = "community_id")
    val communityId: Long,

    @Column(name = "vote_option_id")
    val voteOptionId: Long,
) : BaseEntity()

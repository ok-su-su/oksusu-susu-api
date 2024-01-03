package com.oksusu.susu.post.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "vote_history")
class VoteHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    @Column(name = "post_id")
    val postId: Long,

    @Column(name = "vote_option_id")
    val voteOptionId: Long,
) : BaseEntity()

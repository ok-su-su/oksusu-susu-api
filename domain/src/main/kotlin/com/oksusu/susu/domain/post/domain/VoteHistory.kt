package com.oksusu.susu.domain.post.domain

import com.oksusu.susu.domain.common.BaseEntity
import jakarta.persistence.*

/** 투표 기록 */
@Entity
@Table(name = "vote_history")
class VoteHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 기표자 */
    val uid: Long,

    /** 투표한 게시글 id */
    @Column(name = "post_id")
    val postId: Long,

    /** 투표 옵션 id */
    @Column(name = "vote_option_id")
    var voteOptionId: Long,
) : BaseEntity()

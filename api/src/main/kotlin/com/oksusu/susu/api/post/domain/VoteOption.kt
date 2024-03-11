package com.oksusu.susu.api.post.domain

import com.oksusu.susu.api.domain.BaseEntity
import com.oksusu.susu.api.post.model.VoteOptionWithoutIdModel
import jakarta.persistence.*

/** 투표 옵션 */
@Entity
@Table(name = "vote_option")
class VoteOption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 연관된 투표 id */
    @Column(name = "post_id")
    val postId: Long,

    /** 내용 */
    val content: String,

    /** 순서 */
    val seq: Int,
) : BaseEntity() {
    companion object {
        fun of(model: VoteOptionWithoutIdModel, postId: Long): VoteOption {
            return VoteOption(
                postId = postId,
                content = model.content,
                seq = model.seq
            )
        }
    }
}

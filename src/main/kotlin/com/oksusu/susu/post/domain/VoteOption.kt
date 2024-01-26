package com.oksusu.susu.post.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.post.model.VoteOptionWithoutIdModel
import jakarta.persistence.*

@Entity
@Table(name = "vote_option")
class VoteOption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(name = "post_id")
    val postId: Long,

    val content: String,

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

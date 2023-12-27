package com.oksusu.susu.community.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.community.model.VoteOptionModel
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
) : BaseEntity() {
    companion object {
        fun of(voteOptionModel: VoteOptionModel, communityId: Long): VoteOption {
            return VoteOption(
                communityId = communityId,
                content = voteOptionModel.content,
                seq = voteOptionModel.seq
            )
        }
    }
}

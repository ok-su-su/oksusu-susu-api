package com.oksusu.susu.domain.count.domain

import com.oksusu.susu.domain.common.BaseEntity
import com.oksusu.susu.domain.count.domain.vo.CountTargetType
import com.oksusu.susu.domain.count.domain.vo.CountType
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.VoteOption
import jakarta.persistence.*

/** 카운트 */
@Entity
@Table(name = "count")
class Count(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 카운트 타켓 id */
    @Column(name = "target_id")
    val targetId: Long,

    /** 카운트 타겟 타입 */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "target_type")
    val targetType: CountTargetType,

    /** 카운트 타입 */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "count_type")
    val countType: CountType,

    /** 카운트 */
    var count: Long = 0,
) : BaseEntity() {
    companion object {
        fun toVoteLike(post: Post): Count {
            return Count(
                targetId = post.id,
                targetType = CountTargetType.POST,
                countType = CountType.VOTE
            )
        }

        fun toVoteOptionLike(voteOption: VoteOption): Count {
            return Count(
                targetId = voteOption.id,
                targetType = CountTargetType.VOTE_OPTION,
                countType = CountType.VOTE
            )
        }
    }

    override fun toString(): String {
        return "Count(id=$id, targetId=$targetId, targetType=$targetType, countType=$countType, count=$count)"
    }
}

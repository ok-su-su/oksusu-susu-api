package com.oksusu.susu.count.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.count.domain.vo.CountTargetType
import com.oksusu.susu.count.domain.vo.CountType
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.VoteOption
import jakarta.persistence.*

@Entity
@Table(name = "count")
class Count(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(name = "target_id")
    val targetId: Long,

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "target_type")
    val targetType: CountTargetType,

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "count_type")
    val countType: CountType,

    var count: Long = 0,
) : BaseEntity(){
    companion object{
        fun toVoteLike(post: Post): Count {
            return Count(
                targetId = post.id,
                targetType = CountTargetType.POST,
                countType = CountType.LIKE,
            )
        }

        fun toVoteOptionLike(voteOption: VoteOption): Count{
            return Count(
                targetId = voteOption.id,
                targetType = CountTargetType.VOTE_OPTION,
                countType = CountType.LIKE
            )
        }
    }
}
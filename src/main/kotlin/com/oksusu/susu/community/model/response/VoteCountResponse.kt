package com.oksusu.susu.community.model.response

import com.oksusu.susu.common.annotation.DateFormat
import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.model.VoteCountModel
import com.oksusu.susu.community.model.VoteOptionCountModel
import com.oksusu.susu.community.model.VoteOptionModel
import com.oksusu.susu.user.domain.User
import com.oksusu.susu.user.model.UserProfileModel
import java.time.LocalDateTime

class VoteCountResponse(
    val id: Long,
    val isMine: Boolean,
    val category: CommunityCategory,
    val content: String,
    val count: Int,
    @DateFormat
    val createdAt: LocalDateTime,
    val creatorProfile: UserProfileModel,
    val options: List<VoteOptionCountModel>,
) {
    companion object {
        fun of(
            vote: VoteCountModel,
            options: List<VoteOptionCountModel>,
            creator: User,
            isMine: Boolean
        ): VoteCountResponse {
            return VoteCountResponse(
                id = vote.id,
                isMine = isMine,
                category = vote.category,
                content = vote.content,
                count = vote.count,
                createdAt = vote.createdAt,
                creatorProfile = UserProfileModel.from(creator),
                options = options,
            )
        }
    }
}
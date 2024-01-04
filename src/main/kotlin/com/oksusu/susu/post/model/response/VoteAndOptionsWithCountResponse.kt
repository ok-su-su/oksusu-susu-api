package com.oksusu.susu.post.model.response

import com.oksusu.susu.common.annotation.DateFormat
import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.model.VoteCountModel
import com.oksusu.susu.post.model.VoteOptionCountModel
import com.oksusu.susu.user.domain.User
import com.oksusu.susu.user.model.UserProfileModel
import java.time.LocalDateTime

class VoteAndOptionsWithCountResponse(
    val id: Long,
    val isMine: Boolean,
    val category: String,
    val content: String,
    val count: Int,
    @DateFormat
    val createdAt: LocalDateTime,
    val creatorProfile: UserProfileModel,
    val isModified: Boolean,
    val options: List<VoteOptionCountModel>,
) {
    companion object {
        fun of(
            vote: VoteCountModel,
            options: List<VoteOptionCountModel>,
            creator: User,
            isMine: Boolean,
        ): VoteAndOptionsWithCountResponse {
            return VoteAndOptionsWithCountResponse(
                id = vote.id,
                isMine = isMine,
                category = vote.category,
                content = vote.content,
                count = vote.count,
                createdAt = vote.createdAt,
                creatorProfile = UserProfileModel.from(creator),
                isModified = !vote.createdAt.equalsFromYearToSec(vote.modifiedAt),
                options = options
            )
        }
    }
}

package com.oksusu.susu.post.model.response

import com.oksusu.susu.common.annotation.DateFormat
import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.model.VoteCountModel
import com.oksusu.susu.post.model.VoteOptionCountModel
import com.oksusu.susu.user.domain.User
import com.oksusu.susu.user.model.UserProfileModel
import java.time.LocalDateTime

data class VoteAllInfoResponse(
    /** 투표 id */
    val id: Long,
    /** 본인 소유 여부 */
    val isMine: Boolean,
    /** 카테고리 명 */
    val category: String,
    /** 내용 */
    val content: String,
    /** 총 투표 수 */
    val count: Long,
    /** 생성일 */
    @DateFormat
    val createdAt: LocalDateTime,
    /** 생성자 profile */
    val creatorProfile: UserProfileModel,
    /** 수정 여부 */
    val isModified: Boolean,
    /** 투표 옵션 */
    val options: List<VoteOptionCountModel>,
) {
    companion object {
        fun of(
            vote: VoteCountModel,
            options: List<VoteOptionCountModel>,
            creator: User,
            isMine: Boolean,
        ): VoteAllInfoResponse {
            return VoteAllInfoResponse(
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

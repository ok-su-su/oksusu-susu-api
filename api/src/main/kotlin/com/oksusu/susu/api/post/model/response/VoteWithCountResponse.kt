package com.oksusu.susu.api.post.model.response

import com.oksusu.susu.common.extension.equalsFromYearToSec
import com.oksusu.susu.domain.post.infrastructure.repository.model.PostAndVoteCountModel
import com.oksusu.susu.api.post.model.BoardModel

data class VoteWithCountResponse(
    /** 투표 id */
    val id: Long,
    /** 보드 */
    val board: BoardModel,
    /** 내용 */
    val content: String,
    /** 총 투표 수 */
    val count: Long,
    /** 수정 여부 / 수정함 : true, 수정 안함 : false */
    val isModified: Boolean,
) {
    companion object {
        fun of(model: PostAndVoteCountModel, boardModel: BoardModel): VoteWithCountResponse {
            return VoteWithCountResponse(
                id = model.post.id,
                board = boardModel,
                content = model.post.content,
                count = model.voteCount,
                isModified = model.post.createdAt.equalsFromYearToSec(model.post.modifiedAt)
            )
        }
    }
}

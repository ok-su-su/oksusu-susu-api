package com.oksusu.susu.post.model.response

import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.model.BoardModel
import com.oksusu.susu.post.model.VoteOptionAndHistoryModel
import java.time.LocalDateTime

data class CreateAndUpdateVoteResponse(
    /** 투표 id */
    val id: Long,
    /** 투표 생성자 id */
    val uid: Long,
    /** 보드 */
    val board: BoardModel,
    /** 내용 */
    val content: String,
    /** 수정 여부 / 수정함 : true, 수정 안함 : false */
    val isModified: Boolean,
    /** 투표 옵션 */
    val options: List<VoteOptionAndHistoryModel>,
    /** 투표 생성일 */
    val createdAt: LocalDateTime,
) {
    companion object {
        fun of(
            uid: Long,
            post: Post,
            optionModels: List<VoteOptionAndHistoryModel>,
            boardModel: BoardModel,
        ): CreateAndUpdateVoteResponse {
            return CreateAndUpdateVoteResponse(
                id = post.id,
                uid = uid,
                board = boardModel,
                content = post.content,
                isModified = !post.createdAt.equalsFromYearToSec(post.modifiedAt),
                options = optionModels,
                createdAt = post.createdAt
            )
        }
    }
}

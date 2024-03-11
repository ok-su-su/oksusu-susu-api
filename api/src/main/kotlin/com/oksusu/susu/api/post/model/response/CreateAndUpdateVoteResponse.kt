package com.oksusu.susu.api.post.model.response

import com.oksusu.susu.common.extension.equalsFromYearToSec
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.api.post.model.BoardModel
import com.oksusu.susu.api.post.model.VoteOptionAndHistoryModel
import java.time.LocalDateTime

data class CreateAndUpdateVoteResponse(
    /** 투표 id */
    val id: Long,
    /** 본인 소유 글 여부 / 내 글 : 1, 전체 글 : 0 */
    val isMine: Boolean,
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
            isMine: Boolean,
        ): CreateAndUpdateVoteResponse {
            return CreateAndUpdateVoteResponse(
                id = post.id,
                uid = uid,
                board = boardModel,
                content = post.content,
                isModified = !post.createdAt.equalsFromYearToSec(post.modifiedAt),
                options = optionModels,
                createdAt = post.createdAt,
                isMine = isMine
            )
        }
    }
}

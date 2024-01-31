package com.oksusu.susu.post.model.response

import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.domain.Board
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.model.BoardModel
import com.oksusu.susu.post.model.VoteOptionModel
import java.time.LocalDateTime

class VoteAndOptionsWithCountResponse(
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
    /** 총 투표 수 */
    val count: Long,
    /** 투표 옵션 */
    val options: List<VoteOptionModel>,
    /** 투표 생성일 */
    val createdAt: LocalDateTime,
) {
    companion object {
        fun of(
            vote: Post,
            count: Count,
            options: List<VoteOptionModel>,
            boardModel: BoardModel,
        ): VoteAndOptionsWithCountResponse {
            return VoteAndOptionsWithCountResponse(
                id = vote.id,
                uid = vote.uid,
                board = boardModel,
                content = vote.content,
                isModified = !vote.createdAt.equalsFromYearToSec(vote.modifiedAt),
                count = count.count,
                options = options.filter { it.postId == vote.id },
                createdAt = vote.createdAt
            )
        }
    }
}

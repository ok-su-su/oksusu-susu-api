package com.oksusu.susu.post.model.response

import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.infrastructure.repository.model.PostAndCountModel
import com.oksusu.susu.post.model.BoardModel

data class VoteWithCountResponse(
    /** 투표 id */
    val id: Long,
    /** 보드 명 */
    val boardName: String,
    /** 내용 */
    val content: String,
    /** 총 투표 수 */
    val count: Long,
    /** 수정 여부 / 수정함 : true, 수정 안함 : false */
    val isModified: Boolean,
) {
    companion object {
        fun of(model: PostAndCountModel, boardModel: BoardModel): VoteWithCountResponse {
            return VoteWithCountResponse(
                id = model.post.id,
                boardName = boardModel.name,
                content = model.post.content,
                count = model.count.count,
                isModified = model.post.createdAt.equalsFromYearToSec(model.post.modifiedAt)
            )
        }
    }
}

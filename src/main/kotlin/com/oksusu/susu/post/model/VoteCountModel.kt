package com.oksusu.susu.post.model

import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.post.domain.Post
import java.time.LocalDateTime

/** 투표 + 투표수 모델 */
data class VoteCountModel(
    /** 게시글 id */
    val id: Long,
    /** 작성자 id */
    val uid: Long,
    /** 게시글 카테고리 명 */
    val category: String,
    /** 내용 */
    var content: String,
    /** 생성일 */
    val createdAt: LocalDateTime,
    /** 수정일 */
    val modifiedAt: LocalDateTime,
    /** 카운트 */
    val count: Long,
) {
    companion object {
        fun of(post: Post, count: Count, postCategoryModel: PostCategoryModel): VoteCountModel {
            return VoteCountModel(
                id = post.id,
                uid = post.uid,
                category = postCategoryModel.name,
                content = post.content,
                createdAt = post.createdAt,
                modifiedAt = post.modifiedAt,
                count = count.count
            )
        }
    }
}

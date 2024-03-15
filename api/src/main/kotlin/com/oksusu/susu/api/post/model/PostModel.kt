package com.oksusu.susu.api.post.model

import com.oksusu.susu.domain.post.domain.vo.PostType

/** 게시글 모델 */
data class PostModel(
    /** 게시글 id */
    val id: Long,
    /** 작성자 id */
    val uid: Long,
    /** 보드 id */
    var boardId: Long,
    /** 게시글 타입 */
    val type: PostType,
    /** 제목 */
    val title: String?,
    /** 내용 */
    var content: String,
    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    var isActive: Boolean,
)

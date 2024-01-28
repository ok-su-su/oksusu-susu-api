package com.oksusu.susu.post.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.post.domain.vo.PostType
import jakarta.persistence.*

/** 게시글 */
@Entity
@Table(name = "post")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 작성자 id */
    val uid: Long,

    /** 게시글 카테고리 id */
    @Column(name = "post_category_id")
    var postCategoryId: Long,

    /** 게시글 타입 */
    val type: PostType,

    /** 제목 */
    val title: String? = null,

    /** 내용 */
    var content: String,

    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()

package com.oksusu.susu.post.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.post.domain.vo.PostType
import jakarta.persistence.*

@Entity
@Table(name = "post")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    @Column(name = "post_category_id")
    var postCategoryId: Long,

    val type: PostType,

    val title: String? = null,

    var content: String,

    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()

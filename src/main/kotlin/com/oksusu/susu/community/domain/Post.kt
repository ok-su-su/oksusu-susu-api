package com.oksusu.susu.community.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.community.domain.vo.PostType
import jakarta.persistence.*

@Entity
@Table(name = "post")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    @Column(name = "post_category_id")
    val postCategoryId: Long,

    val type: PostType,

    val title: String? = null,

    val content: String,

    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()

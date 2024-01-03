package com.oksusu.susu.post.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "post_category")
class PostCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val name: String,

    val seq: Int,

    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()

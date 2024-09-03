package com.oksusu.susu.domain.category.domain

import com.oksusu.susu.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "category")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val seq: Long,

    val name: String,

    val style: String,

    @Column(name = "is_active")
    val isActive: Boolean,

    @Column(name = "is_custom")
    val isCustom: Boolean = false,
) : BaseEntity()

package com.oksusu.susu.community.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.domain.vo.CommunityType
import jakarta.persistence.*

@Entity
@Table(name = "community")
class Community(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val type: CommunityType,

    val title: String,

    val content: String,

    val category: CommunityCategory,

    @Column(name = "is_active")
    val isActive: Boolean,
) : BaseEntity()
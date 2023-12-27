package com.oksusu.susu.community.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.community.domain.vo.CommunityType
import jakarta.persistence.*

@Entity
@Table(name = "community")
class Community(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    val type: CommunityType,

    val title: String? = null,

    val content: String,

    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()

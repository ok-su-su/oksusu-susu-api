package com.oksusu.susu.domain.friend.domain

import com.oksusu.susu.domain.common.BaseEntity
import jakarta.persistence.*

/** 관계 */
@Entity
@Table(name = "relationship")
class Relationship(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 관계 */
    val relation: String,

    /** 상세 설명 */
    val description: String,

    /** 활성화 : 1, 비활성화 : 0 */
    @Column(name = "is_active")
    val isActive: Boolean,

    /** 커스텀 여부 */
    @Column(name = "is_custom")
    val isCustom: Boolean = false,
) : BaseEntity()

package com.oksusu.susu.friend.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

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
) : BaseEntity()

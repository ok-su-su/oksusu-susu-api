package com.oksusu.susu.domain.term.domain

import com.oksusu.susu.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/** 약관 정보 */
@Entity
@Table(name = "term")
class Term(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 약관 제목 */
    val title: String,

    /** 약관 내용 */
    val description: String?,

    /** 약관 순서 */
    val seq: Int,

    /** 필수 동의 여부 / 필수 : 1, 선택 : 0 */
    @Column(name = "is_essential")
    val isEssential: Boolean,

    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    @Column(name = "is_active")
    val isActive: Boolean,
) : BaseEntity()

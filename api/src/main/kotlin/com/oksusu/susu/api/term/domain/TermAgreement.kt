package com.oksusu.susu.api.term.domain

import com.oksusu.susu.api.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/** 약관 동의 정보 */
@Entity
@Table(name = "term_agreement")
class TermAgreement(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 약관 동의한 유저 id */
    val uid: Long,

    /** 약관 정보 id */
    @Column(name = "term_id")
    val termId: Long,

    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()

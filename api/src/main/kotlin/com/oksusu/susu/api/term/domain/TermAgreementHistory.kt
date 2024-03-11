package com.oksusu.susu.api.term.domain

import com.oksusu.susu.api.domain.BaseEntity
import com.oksusu.susu.api.term.domain.vo.TermAgreementChangeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/** 약관 동의 정보 기록 */
@Entity
@Table(name = "term_agreement_history")
class TermAgreementHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 약관 동의한 유저 id */
    val uid: Long,

    /** 약관 정보 id */
    @Column(name = "term_id")
    val termId: Long,

    /** 약관 동의 여부 변화 종류 */
    @Column(name = "change_type")
    @Enumerated
    val changeType: TermAgreementChangeType,
) : BaseEntity()

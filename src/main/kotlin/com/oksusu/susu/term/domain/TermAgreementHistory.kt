package com.oksusu.susu.term.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.term.domain.vo.TermAgreementChangeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "term_agreement_history")
class TermAgreementHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    @Column(name = "term_id")
    val termId: Long,

    @Column(name = "change_type")
    @Enumerated
    val changeType: TermAgreementChangeType,
) : BaseEntity()

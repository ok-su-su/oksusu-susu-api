package com.oksusu.susu.term.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "term")
class TermAssignment (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    @Column(name = "term_id")
    val termId: Long,
) : BaseEntity()
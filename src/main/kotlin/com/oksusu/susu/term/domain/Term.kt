package com.oksusu.susu.term.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "term")
class Term(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val title: String,

    val description: String,

    @Column(name = "is_essential")
    val isEssential: Boolean,

    @Column(name = "is_active")
    val isActive: Boolean,
) : BaseEntity()

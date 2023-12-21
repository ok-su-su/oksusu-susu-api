package com.oksusu.susu.ledger.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "ledger")
class Ledger(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    val title: String,

    val description: String? = null,

    @Column(name = "start_at")
    val startAt: LocalDateTime,

    @Column(name = "end_at")
    val endAt: LocalDateTime,
) : BaseEntity()

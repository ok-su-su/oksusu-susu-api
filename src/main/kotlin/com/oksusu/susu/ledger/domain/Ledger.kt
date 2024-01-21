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

    var title: String,

    var description: String? = null,

    @Column(name = "total_sent_amounts")
    var totalSentAmounts: Long = 0,

    @Column(name = "total_received_amounts")
    var totalReceivedAmounts: Long = 0,

    @Column(name = "start_at")
    var startAt: LocalDateTime,

    @Column(name = "end_at")
    var endAt: LocalDateTime,
) : BaseEntity()

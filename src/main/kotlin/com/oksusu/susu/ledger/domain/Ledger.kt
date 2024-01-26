package com.oksusu.susu.ledger.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/** 장부 */
@Entity
@Table(name = "ledger")
class Ledger(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 장부 소유자 */
    val uid: Long,

    /** 제목 */
    var title: String,

    /** 상세 설명 */
    var description: String? = null,

    /** 보낸 봉투 총합 */
    @Column(name = "total_sent_amounts")
    var totalSentAmounts: Long = 0,

    /** 받은 봉투 총합 */
    @Column(name = "total_received_amounts")
    var totalReceivedAmounts: Long = 0,

    /** 시작일 */
    @Column(name = "start_at")
    var startAt: LocalDateTime,

    /** 종료일 */
    @Column(name = "end_at")
    var endAt: LocalDateTime,
) : BaseEntity()

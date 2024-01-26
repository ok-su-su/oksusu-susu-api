package com.oksusu.susu.envelope.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/** 봉투 */
@Entity
@Table(name = "envelope")
class Envelope(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** user id */
    val uid: Long,

    /** type: SENT, RECEIVED */
    @Enumerated(EnumType.STRING)
    var type: EnvelopeType,

    /** 지인 id */
    @Column(name = "friend_id")
    var friendId: Long,

    /** 장부 id */
    @Column(name = "ledger_id")
    val ledgerId: Long? = null,

    /** 금액 */
    @Column(name = "amount")
    var amount: Long,

    /** 선물 */
    var gift: String? = null,

    /** 메모 */
    var memo: String? = null,

    /** 방문 : 1, 미방문 : 0, null인 경우 미선택 */
    @Column(name = "has_visited")
    var hasVisited: Boolean? = null,

    /** 전달일 */
    @Column(name = "handed_over_at")
    var handedOverAt: LocalDateTime,
) : BaseEntity()

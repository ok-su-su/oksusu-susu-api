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

@Entity
@Table(name = "envelope")
class Envelope(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    @Enumerated(EnumType.STRING)
    var type: EnvelopeType,

    @Column(name = "friend_id")
    var friendId: Long,

    @Column(name = "ledger_id")
    val ledgerId: Long? = null,

    @Column(name = "amount")
    var amount: Long,

    var gift: String? = null,

    var memo: String? = null,

    @Column(name = "has_visited")
    var hasVisited: Boolean,

    @Column(name = "handed_over_at")
    var handedOverAt: LocalDateTime,
) : BaseEntity()

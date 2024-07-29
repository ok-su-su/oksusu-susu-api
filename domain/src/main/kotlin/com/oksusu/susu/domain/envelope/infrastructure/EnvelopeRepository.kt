package com.oksusu.susu.domain.envelope.infrastructure

import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
@Repository
interface EnvelopeRepository : JpaRepository<Envelope, Long>, EnvelopeQRepository {
    fun findByIdAndUid(id: Long, uid: Long): Envelope?

    fun deleteAllByLedgerId(ledgerId: Long)

    fun findAllByLedgerId(ledgerId: Long): List<Envelope>

    fun findTop1ByUidAndTypeOrderByAmountDesc(uid: Long, type: EnvelopeType): Envelope?

    fun findTop1ByUidAndTypeOrderByAmountAsc(uid: Long, type: EnvelopeType): Envelope?

    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long

    fun deleteAllByFriendIdIn(friendIds: List<Long>)

    fun countByUidAndFriendId(uid: Long, friendId: Long): Long

    fun findAllByUidIn(uid: List<Long>): List<Envelope>
}

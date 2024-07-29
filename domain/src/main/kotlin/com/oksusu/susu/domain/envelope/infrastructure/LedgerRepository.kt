package com.oksusu.susu.domain.envelope.infrastructure

import com.oksusu.susu.domain.envelope.domain.Ledger
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
@Repository
interface LedgerRepository : JpaRepository<Ledger, Long>, LedgerQRepository {
    fun findAllByUidAndIdIn(uid: Long, ids: List<Long>): List<Ledger>

    fun findByIdAndUid(id: Long, uid: Long): Ledger?

    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long

    fun findAllByUidIn(uid: List<Long>): List<Ledger>
}

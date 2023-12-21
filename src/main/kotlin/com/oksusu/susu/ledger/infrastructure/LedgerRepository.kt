package com.oksusu.susu.ledger.infrastructure

import com.oksusu.susu.ledger.domain.Ledger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LedgerRepository : JpaRepository<Ledger, Long> {
    @Transactional(readOnly = true)
    fun findAllByUid(uid: Long, pageable: Pageable): Page<Ledger>
}

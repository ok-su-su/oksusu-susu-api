package com.oksusu.susu.domain.term.infrastructure

import com.oksusu.susu.domain.term.domain.Term
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface TermRepository : JpaRepository<Term, Long> {
    fun findAllByIsActive(isActive: Boolean): List<Term>

    fun countAllByIdIn(ids: List<Long>): Long
}

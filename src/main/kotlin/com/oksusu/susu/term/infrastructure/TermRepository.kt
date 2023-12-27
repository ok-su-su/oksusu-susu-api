package com.oksusu.susu.term.infrastructure

import com.oksusu.susu.term.domain.Term
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TermRepository: JpaRepository<Term, Long> {
    fun findAllByIsActive(isActive: Boolean): List<Term>
}
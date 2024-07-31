package com.oksusu.susu.domain.term.infrastructure

import com.oksusu.susu.domain.term.domain.TermAgreementHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface TermAgreementHistoryRepository : JpaRepository<TermAgreementHistory, Long>

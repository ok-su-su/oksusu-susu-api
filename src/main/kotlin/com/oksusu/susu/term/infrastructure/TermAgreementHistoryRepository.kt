package com.oksusu.susu.term.infrastructure

import com.oksusu.susu.term.domain.TermAgreementHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TermAgreementHistoryRepository : JpaRepository<TermAgreementHistory, Long>

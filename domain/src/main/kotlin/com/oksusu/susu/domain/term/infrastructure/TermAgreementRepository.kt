package com.oksusu.susu.domain.term.infrastructure

import com.oksusu.susu.domain.term.domain.TermAgreement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface TermAgreementRepository : JpaRepository<TermAgreement, Long>

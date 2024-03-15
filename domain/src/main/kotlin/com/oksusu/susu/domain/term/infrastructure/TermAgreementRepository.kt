package com.oksusu.susu.domain.term.infrastructure

import com.oksusu.susu.domain.term.domain.TermAgreement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TermAgreementRepository : JpaRepository<TermAgreement, Long>

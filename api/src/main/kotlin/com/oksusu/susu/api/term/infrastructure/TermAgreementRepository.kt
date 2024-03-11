package com.oksusu.susu.api.term.infrastructure

import com.oksusu.susu.api.term.domain.TermAgreement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TermAgreementRepository : JpaRepository<TermAgreement, Long>

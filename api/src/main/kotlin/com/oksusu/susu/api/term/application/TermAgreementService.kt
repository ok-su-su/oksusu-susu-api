package com.oksusu.susu.api.term.application

import com.oksusu.susu.domain.term.domain.TermAgreement
import com.oksusu.susu.domain.term.infrastructure.TermAgreementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TermAgreementService(
    private val termAgreementRepository: TermAgreementRepository,
) {
    @Transactional
    fun saveAllSync(assignments: List<TermAgreement>): List<TermAgreement> {
        return termAgreementRepository.saveAll(assignments)
    }
}

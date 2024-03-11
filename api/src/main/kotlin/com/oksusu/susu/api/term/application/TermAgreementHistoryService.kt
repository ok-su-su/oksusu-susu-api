package com.oksusu.susu.api.term.application

import com.oksusu.susu.domain.term.domain.TermAgreementHistory
import com.oksusu.susu.domain.term.infrastructure.TermAgreementHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TermAgreementHistoryService(
    private val termAgreementHistoryRepository: TermAgreementHistoryRepository,
) {
    @Transactional
    fun saveAllSync(termAgreementHistory: List<TermAgreementHistory>): List<TermAgreementHistory> {
        return termAgreementHistoryRepository.saveAll(termAgreementHistory)
    }
}

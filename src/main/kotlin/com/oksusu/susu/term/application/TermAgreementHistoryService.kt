package com.oksusu.susu.term.application

import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.term.domain.TermAgreementHistory
import com.oksusu.susu.term.infrastructure.TermAgreementHistoryRepository
import com.oksusu.susu.term.model.event.TermAgreementHistoryCreateEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class TermAgreementHistoryService(
    private val termAgreementHistoryRepository: TermAgreementHistoryRepository,
    private val txTemplates: TransactionTemplates
) {
    val logger = mu.KotlinLogging.logger { }

    @Transactional
    fun saveAllSync(termAgreementHistory: List<TermAgreementHistory>): List<TermAgreementHistory> {
        return termAgreementHistoryRepository.saveAll(termAgreementHistory)
    }

    @Async
    @TransactionalEventListener
    fun createTermAgreementHistoryService(event: TermAgreementHistoryCreateEvent) {
        val uid = event.termAgreements.first().uid
        val termIds = event.termAgreements.map { it.termId }
        logger.info { "${event.publishAt}에 발행된 $uid 유저의 $termIds 번 term agreement history ${event.changeType} 실행 시작" }
        CoroutineScope(Dispatchers.IO).launch {
            termIds.map {
                TermAgreementHistory(
                    uid = uid,
                    termId = it,
                    changeType = event.changeType
                )
            }.run { saveAllSync(this) }
        }
        logger.info { "${event.publishAt}에 발행된 $uid 유저의 $termIds 번 term agreement history ${event.changeType} 실행 완료" }
    }
}
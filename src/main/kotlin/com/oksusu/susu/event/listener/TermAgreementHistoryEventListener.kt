package com.oksusu.susu.event.listener

import com.oksusu.susu.event.model.TermAgreementHistoryCreateEvent
import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.term.application.TermAgreementHistoryService
import com.oksusu.susu.term.domain.TermAgreementHistory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TermAgreementHistoryEventListener(
    private val termAgreementHistoryService: TermAgreementHistoryService,
) {
    val logger = KotlinLogging.logger { }

    @TransactionalEventListener
    fun createTermAgreementHistoryService(event: TermAgreementHistoryCreateEvent) {
        CoroutineScope(Dispatchers.IO.withMDCContext()).launch {
            val uid = event.termAgreements.first().uid
            val termIds = event.termAgreements.map { it.termId }
            logger.info { "${event.publishAt}에 발행된 $uid 유저의 $termIds 번 term agreement history ${event.changeType} 실행 시작" }

            termIds.map {
                TermAgreementHistory(
                    uid = uid,
                    termId = it,
                    changeType = event.changeType
                )
            }.run { termAgreementHistoryService.saveAllSync(this) }

            logger.info { "${event.publishAt}에 발행된 $uid 유저의 $termIds 번 term agreement history ${event.changeType} 실행 완료" }
        }
    }
}

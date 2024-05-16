package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.event.model.TermAgreementHistoryCreateEvent
import com.oksusu.susu.api.term.application.TermAgreementHistoryService
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.domain.term.domain.TermAgreementHistory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
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

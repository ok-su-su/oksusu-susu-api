package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.category.application.CategoryAssignmentService
import com.oksusu.susu.domain.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.api.envelope.application.EnvelopeService
import com.oksusu.susu.api.event.model.DeleteLedgerEvent
import com.oksusu.susu.domain.common.extension.coExecuteOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class LedgerEventListener(
    private val envelopeService: EnvelopeService,
    private val categoryAssignmentService: CategoryAssignmentService,
    private val txTemplates: TransactionTemplates,
) {
    @TransactionalEventListener
    fun handel(event: DeleteLedgerEvent) {
        CoroutineScope(Dispatchers.IO + Job()).launch {
            val envelopes = envelopeService.findAllByLedgerId(event.ledger.id)
            val envelopeIds = envelopes.map { envelope -> envelope.id }

            txTemplates.writer.coExecuteOrNull {
                /** 봉투 삭제 */
                envelopeService.deleteAllByLedgerId(event.ledger.id)

                /** 카테고리 삭제 */
                categoryAssignmentService.deleteAllByTargetTypeAndTargetIdIn(
                    targetType = CategoryAssignmentType.ENVELOPE,
                    targetIds = envelopeIds
                )
            }
        }
    }
}

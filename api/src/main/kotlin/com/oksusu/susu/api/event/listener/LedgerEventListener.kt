package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.category.application.CategoryAssignmentService
import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.envelope.application.EnvelopeService
import com.oksusu.susu.api.event.model.DeleteLedgerEvent
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.domain.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.domain.common.extension.coExecuteOrNull
import com.oksusu.susu.domain.config.database.TransactionTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.transaction.event.TransactionalEventListener

@SusuEventListener
class LedgerEventListener(
    private val envelopeService: EnvelopeService,
    private val categoryAssignmentService: CategoryAssignmentService,
    private val txTemplates: TransactionTemplates,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    @TransactionalEventListener
    fun handel(event: DeleteLedgerEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
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

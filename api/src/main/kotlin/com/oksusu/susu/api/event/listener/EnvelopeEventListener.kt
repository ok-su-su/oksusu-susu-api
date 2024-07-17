package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.envelope.application.EnvelopeService
import com.oksusu.susu.api.event.model.DeleteEnvelopeEvent
import com.oksusu.susu.api.friend.application.FriendRelationshipService
import com.oksusu.susu.api.friend.application.FriendService
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.domain.envelope.infrastructure.LedgerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.event.TransactionalEventListener

@SusuEventListener
class EnvelopeEventListener(
    private val envelopeService: EnvelopeService,
    private val friendService: FriendService,
    private val ledgerRepository: LedgerRepository,
    private val friendRelationshipService: FriendRelationshipService,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    @TransactionalEventListener
    fun handel(event: DeleteEnvelopeEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
            val count = envelopeService.countByUidAndFriendId(
                uid = event.uid,
                friendId = event.envelope.friendId
            )

            /** ledger의 봉투 합계 총합 업데이트 */
            if (event.envelope.ledgerId != null) {
                val ledger = ledgerRepository.findByIdOrNull(event.envelope.ledgerId)

                if (ledger != null) {
                    val countTotalAmountsAndCountsModel = envelopeService.countTotalAmountAndCount(ledger.id)

                    ledger.apply {
                        this.totalSentAmounts = countTotalAmountsAndCountsModel.totalSentAmounts
                        this.totalReceivedAmounts = countTotalAmountsAndCountsModel.totalReceivedAmounts
                    }.run { ledgerRepository.save(this) }
                }
            }

            /** 조회되는 케이스가 없는 경우, 친구정보도 삭제 */
            if (count == 0L) {
                friendService.deleteSync(event.envelope.friendId)
                friendRelationshipService.deleteByFriendIdSync(event.envelope.friendId)
            }
        }
    }
}

package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.envelope.application.EnvelopeService
import com.oksusu.susu.api.envelope.application.LedgerService
import com.oksusu.susu.api.event.model.CreateEnvelopeEvent
import com.oksusu.susu.api.event.model.DeleteEnvelopeEvent
import com.oksusu.susu.api.event.model.UpdateEnvelopeEvent
import com.oksusu.susu.api.friend.application.FriendRelationshipService
import com.oksusu.susu.api.friend.application.FriendService
import com.oksusu.susu.api.statistic.application.UserEnvelopeStatisticService
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.common.extension.parZipWithMDC
import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.domain.envelope.infrastructure.LedgerRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.event.TransactionalEventListener

@SusuEventListener
class EnvelopeEventListener(
    private val envelopeService: EnvelopeService,
    private val ledgerService: LedgerService,
    private val friendService: FriendService,
    private val ledgerRepository: LedgerRepository,
    private val friendRelationshipService: FriendRelationshipService,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
    private val userEnvelopeStatisticService: UserEnvelopeStatisticService,
) {
    private val logger = KotlinLogging.logger { }

    @TransactionalEventListener
    fun handle(event: CreateEnvelopeEvent) {
        /** 장부 처리 집계 변경 */
        event.ledger?.let { ledger ->
            when (event.envelope.type) {
                EnvelopeType.RECEIVED -> {
                    ledger.totalReceivedAmounts += event.envelope.amount
                }

                EnvelopeType.SENT -> {
                    ledger.totalSentAmounts += event.envelope.amount
                }
            }

            ledgerService.saveSync(ledger)
        }

        /** 통계 캐싱 처리 */
        mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
            /** 통계 캐싱 안되어있으면 중단 */
            userEnvelopeStatisticService.getStatisticOrNull(event.user.uid) ?: run { return@launch }

            logger.info { "[${event.publishAt}] ${event.user.uid} refresh user envelope statistic 시작" }

            val statistic = userEnvelopeStatisticService.createUserEnvelopeStatistic(event.user.uid)

            userEnvelopeStatisticService.save(event.user.uid, statistic)

            logger.info { "[${event.publishAt}] ${event.user.uid} refresh user envelope statistic 끝" }
        }
    }

    @TransactionalEventListener
    fun handle(event: UpdateEnvelopeEvent) {
        event.envelope.ledgerId?.let { ledgerId ->
            mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
                parZipWithMDC(
                    { envelopeService.countTotalAmountAndCount(ledgerId) },
                    { ledgerService.findByIdAndUidOrThrow(ledgerId, event.envelope.uid) }
                ) { countTotalAmountsAndCountsModel, ledger ->
                    ledger.apply {
                        this.totalSentAmounts = countTotalAmountsAndCountsModel.totalSentAmounts
                        this.totalReceivedAmounts = countTotalAmountsAndCountsModel.totalReceivedAmounts
                    }

                    ledgerRepository.save(ledger)
                }
            }
        }

        /** 통계 캐싱 처리 */
        mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
            /** 통계 캐싱 안되어있으면 중단 */
            userEnvelopeStatisticService.getStatisticOrNull(event.user.uid) ?: run { return@launch }

            logger.info { "[${event.publishAt}] ${event.user.uid} refresh user envelope statistic 시작" }

            val statistic = userEnvelopeStatisticService.createUserEnvelopeStatistic(event.user.uid)

            userEnvelopeStatisticService.save(event.user.uid, statistic)

            logger.info { "[${event.publishAt}] ${event.user.uid} refresh user envelope statistic 끝" }
        }
    }

    @TransactionalEventListener
    fun handel(event: DeleteEnvelopeEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
            val count = envelopeService.countByUidAndFriendId(
                uid = event.user.uid,
                friendId = event.envelope.friendId
            )

            /** ledger의 봉투 합계 총합 업데이트 */
            event.envelope.ledgerId?.let { ledgerId ->
                val ledger = ledgerRepository.findByIdOrNull(ledgerId)

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

        /** 통계 캐싱 처리 */
        mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
            /** 통계 캐싱 안되어있으면 중단 */
            userEnvelopeStatisticService.getStatisticOrNull(event.user.uid) ?: run { return@launch }

            logger.info { "[${event.publishAt}] ${event.user.uid} refresh user envelope statistic 시작" }

            val statistic = userEnvelopeStatisticService.createUserEnvelopeStatistic(event.user.uid)

            userEnvelopeStatisticService.save(event.user.uid, statistic)

            logger.info { "[${event.publishAt}] ${event.user.uid} refresh user envelope statistic 끝" }
        }
    }
}

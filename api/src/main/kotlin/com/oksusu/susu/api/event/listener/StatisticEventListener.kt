package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.event.model.CacheUserEnvelopeStatisticEvent
import com.oksusu.susu.api.event.model.RefreshUserEnvelopeStatisticEvent
import com.oksusu.susu.api.statistic.application.UserEnvelopeStatisticService
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.mdcCoroutineScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.transaction.event.TransactionalEventListener

@SusuEventListener
class StatisticEventListener(
    private val userEnvelopeStatisticService: UserEnvelopeStatisticService,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    private val logger = KotlinLogging.logger { }

    @EventListener
    fun cacheUserEnvelopStatistic(event: CacheUserEnvelopeStatisticEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
            logger.info { "[${event.publishAt}] ${event.uid} 유저 봉투 통계 캐싱 시작" }

            userEnvelopeStatisticService.save(event.uid, event.statistic)

            logger.info { "[${event.publishAt}] ${event.uid} 유저 봉투 통계 캐싱 끝" }
        }
    }

    @TransactionalEventListener
    fun createUserWithdrawService(event: RefreshUserEnvelopeStatisticEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
            /** 통계 캐싱 안되어있으면 중단 */
            userEnvelopeStatisticService.getStatisticOrNull(event.uid) ?: run { return@launch }

            logger.info { "[${event.publishAt}] ${event.uid} refresh user envelope statistic 시작" }

            val statistic = userEnvelopeStatisticService.createUserEnvelopeStatistic(event.uid)

            userEnvelopeStatisticService.save(event.uid, statistic)

            logger.info { "[${event.publishAt}] ${event.uid} refresh user envelope statistic 끝" }
        }
    }
}

package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.count.application.CountService
import com.oksusu.susu.api.event.model.DeleteVoteCountEvent
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.domain.common.extension.coExecuteOrNull
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.domain.count.domain.vo.CountTargetType
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class CountEventListener(
    private val countService: CountService,
    private val txTemplates: TransactionTemplates,
) {
    val logger = KotlinLogging.logger { }

    @TransactionalEventListener
    fun deleteCount(event: DeleteVoteCountEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
            logger.info { "[${event.publishAt}] ${event.postId} post 관련 count delete 시작" }

            txTemplates.writer.coExecuteOrNull {
                /** post count 삭제 */
                countService.deleteByTargetIdAndTargetType(event.postId, CountTargetType.POST)

                /** vote option count 삭제 */
                countService.deleteAllByTargetTypeAndTargetIdIn(CountTargetType.VOTE_OPTION, event.optionIds)
            }

            logger.info { "[${event.publishAt}] ${event.postId} post 관련 count delete 끝" }
        }
    }
}

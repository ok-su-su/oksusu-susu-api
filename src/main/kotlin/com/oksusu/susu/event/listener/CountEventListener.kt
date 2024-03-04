package com.oksusu.susu.event.listener

import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.count.application.CountService
import com.oksusu.susu.count.domain.vo.CountTargetType
import com.oksusu.susu.event.model.DeleteVoteCountEvent
import com.oksusu.susu.extension.coExecuteOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
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
        CoroutineScope(Dispatchers.IO + Job()).launch {
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

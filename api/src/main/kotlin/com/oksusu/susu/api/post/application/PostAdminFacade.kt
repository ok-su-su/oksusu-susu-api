package com.oksusu.susu.api.post.application

import com.oksusu.susu.api.auth.model.AdminUser
import com.oksusu.susu.api.event.model.DeleteVoteCountEvent
import com.oksusu.susu.common.extension.parZipWithMDC
import com.oksusu.susu.domain.common.extension.coExecute
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.domain.post.domain.vo.PostType
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class PostAdminFacade(
    private val txTemplates: TransactionTemplates,
    private val postService: PostService,
    private val voteService: VoteService,
    private val voteOptionService: VoteOptionService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun deletePost(user: AdminUser, type: PostType, id: Long) {
        logger.info { "${user.uid} admin이 $id $type 삭제 실행 " }

        when (type) {
            PostType.VOTE -> deleteVote(id)
        }
    }

    private suspend fun deleteVote(id: Long) {
        val (vote, options) = parZipWithMDC(
            { voteService.getVote(id) },
            { voteOptionService.getVoteOptions(id) }
        ) { vote, options -> vote to options }
        val optionIds = options.map { option -> option.id }

        txTemplates.writer.coExecute(Dispatchers.IO + MDCContext()) {
            vote.apply { isActive = false }.run {
                postService.saveSync(this)
            }

            eventPublisher.publishEvent(
                DeleteVoteCountEvent(
                    postId = vote.id,
                    optionIds = optionIds
                )
            )
        }
    }
}

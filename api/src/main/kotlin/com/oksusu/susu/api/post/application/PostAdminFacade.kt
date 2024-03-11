package com.oksusu.susu.api.post.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.api.auth.model.AdminUser
import com.oksusu.susu.api.config.database.TransactionTemplates
import com.oksusu.susu.api.event.model.DeleteVoteCountEvent
import com.oksusu.susu.api.extension.coExecute
import com.oksusu.susu.api.post.domain.vo.PostType
import io.github.oshai.kotlinlogging.KotlinLogging
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
        val (vote, options) = parZip(
            { voteService.getVote(id) },
            { voteOptionService.getVoteOptions(id) }
        ) { vote, options -> vote to options }

        txTemplates.writer.coExecute {
            vote.apply { isActive = false }.run {
                postService.saveSync(this)
            }

            eventPublisher.publishEvent(
                DeleteVoteCountEvent(
                    postId = vote.id,
                    optionIds = options.map { option -> option.id }
                )
            )
        }
    }
}

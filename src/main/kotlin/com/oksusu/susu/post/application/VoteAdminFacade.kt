package com.oksusu.susu.post.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.event.model.DeleteVoteCountEvent
import com.oksusu.susu.extension.coExecute
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class VoteAdminFacade(
    private val txTemplates: TransactionTemplates,
    private val postService: PostService,
    private val voteService: VoteService,
    private val voteOptionService: VoteOptionService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun deleteVote(user: AuthUser, id: Long) {
        logger.info { "${user.uid} admin이 $id vote 삭제 실행 " }

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

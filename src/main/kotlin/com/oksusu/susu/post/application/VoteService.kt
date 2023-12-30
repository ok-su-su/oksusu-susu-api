package com.oksusu.susu.post.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NoAuthorityException
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.PostRepository
import com.oksusu.susu.post.infrastructure.repository.model.PostAndVoteOptionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service

@Service
class VoteService(
    private val postService: PostService,
    private val postRepository: PostRepository,
    private val txTemplates: TransactionTemplates,
) {
    val logger = mu.KotlinLogging.logger { }

    suspend fun getAllVotes(
        isMine: Boolean,
        uid: Long,
        categoryId: Long,
        pageable: Pageable,
    ): Slice<Post> {
        return withContext(Dispatchers.IO) {
            postRepository.getAllVotes(isMine, uid, categoryId, pageable)
        }
    }

    suspend fun getVote(id: Long): Post {
        return postService.findByIdAndIsActiveAndTypeOrThrow(id, true, PostType.VOTE)
    }

    suspend fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel> {
        return withContext(Dispatchers.IO) {
            postRepository.getVoteAndOptions(id)
        }.takeUnless { it.isEmpty() } ?: throw NotFoundException(ErrorCode.NOT_FOUND_VOTE_ERROR)
    }

    suspend fun softDeleteVote(uid: Long, id: Long) {
        val vote = getVote(id)

        if (vote.uid != uid) {
            throw NoAuthorityException(ErrorCode.NO_AUTHORITY_ERROR)
        }

        vote.apply { isActive = false }

        txTemplates.writer.executeWithContext {
            postService.saveSync(vote)
        }
    }

    suspend fun getAllVotesByIdIn(postIds: List<Long>): List<Post> {
        return postService.findByIsActiveAndTypeAndIdIn(true, PostType.VOTE, postIds)
    }

    suspend fun getAllVotesOrderByPopular(
        isMine: Boolean,
        uid: Long,
        categoryId: Long,
        ids: List<Long>,
        pageable: Pageable,
    ): Slice<Post> {
        val (votes, totalCount) = parZip(
            Dispatchers.IO,
            { postRepository.getAllVotesOrderByPopular(isMine, uid, categoryId, ids) },
            { getActiveVoteCount() },
            { a, b -> a to b }
        )

        val sortedContent = ids.flatMap { id -> votes.filter { it.id == id } }
        val listSize = sortedContent.size.takeIf { sortedContent.size < pageable.pageSize } ?: pageable.pageSize
        val hasNext = totalCount > (pageable.pageNumber + 1) * pageable.pageSize
        return SliceImpl(sortedContent.subList(0, listSize), pageable, hasNext)
    }

    suspend fun getActiveVoteCount(): Long {
        return postService.countAllByIsActiveAndType(true, PostType.VOTE)
    }
}

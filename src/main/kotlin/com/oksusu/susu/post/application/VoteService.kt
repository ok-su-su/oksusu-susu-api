package com.oksusu.susu.post.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NoAuthorityException
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.PostRepository
import com.oksusu.susu.post.infrastructure.repository.model.PostAndVoteOptionModel
import com.oksusu.susu.post.infrastructure.repository.model.SearchVoteSpec
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

    suspend fun getAllVotesExceptBlock(
        uid: Long,
        searchSpec: SearchVoteSpec,
        userBlockIds: List<Long>,
        postBlockIds: List<Long>,
        pageable: Pageable,
    ): Slice<Post> {
        return withContext(Dispatchers.IO) {
            postRepository.getAllVotesExceptBlock(
                uid = uid,
                searchSpec = searchSpec,
                userBlockIds = userBlockIds,
                postBlockIds = postBlockIds,
                pageable = pageable
            )
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

        val softDeletedVote = vote.apply { isActive = false }

        txTemplates.writer.coExecute {
            postService.saveSync(softDeletedVote)
        }
    }

    suspend fun getAllVotesByIdInExceptBlock(
        postIds: List<Long>,
        userBlockId: List<Long>,
        postBlockIds: List<Long>,
    ): List<Post> {
        return postService.findByIsActiveAndTypeAndIdInExceptBlock(
            isActive = true,
            type = PostType.VOTE,
            ids = postIds,
            userBlockId = userBlockId,
            postBlockIds = postBlockIds
        )
    }

    suspend fun getAllVotesOrderByPopular(
        uid: Long,
        searchSpec: SearchVoteSpec,
        ids: List<Long>,
        userBlockIds: List<Long>,
        postBlockIds: List<Long>,
        pageable: Pageable,
    ): Slice<Post> {
        return parZip(
            {
                postRepository.getAllVotesOrderByPopular(
                    uid = uid,
                    searchSpec = searchSpec,
                    ids = ids,
                    userBlockIds = userBlockIds,
                    postBlockIds = postBlockIds
                )
            },
            { getActiveVoteCount() }
        ) { votes, totalCount ->
            val sortedContent = ids.flatMap { id -> votes.filter { it.id == id } }
            val listSize = sortedContent.size.takeIf { sortedContent.size < pageable.pageSize } ?: pageable.pageSize
            val hasNext = totalCount > (pageable.pageNumber + 1) * pageable.pageSize

            SliceImpl(sortedContent.subList(0, listSize), pageable, hasNext)
        }
    }

    suspend fun getActiveVoteCount(): Long {
        return postService.countAllByIsActiveAndType(true, PostType.VOTE)
    }
}

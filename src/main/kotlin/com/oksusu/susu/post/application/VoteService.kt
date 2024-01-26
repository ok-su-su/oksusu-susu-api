package com.oksusu.susu.post.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.PostRepository
import com.oksusu.susu.post.infrastructure.repository.model.GetAllVoteSpec
import com.oksusu.susu.post.infrastructure.repository.model.PostAndCountModel
import com.oksusu.susu.post.infrastructure.repository.model.PostAndVoteOptionModel
import com.oksusu.susu.post.infrastructure.repository.model.SearchVoteSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class VoteService(
    private val postService: PostService,
    private val postRepository: PostRepository,
) {
    val logger = mu.KotlinLogging.logger { }

    suspend fun getAllVotesExceptBlock(getAllVoteSpec: GetAllVoteSpec): Slice<PostAndCountModel> {
        return withContext(Dispatchers.IO) {
            postRepository.getAllVotesExceptBlock(getAllVoteSpec)
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

    suspend fun getPopularVotesExceptBlock(
        uid: Long,
        userBlockIds: Set<Long>,
        postBlockIds: Set<Long>,
        size: Int,
    ): List<PostAndCountModel> {
        val spec = GetAllVoteSpec(
            uid = uid,
            searchSpec = SearchVoteSpec.defaultPopularSpec(),
            userBlockIds = userBlockIds,
            postBlockIds = postBlockIds,
            pageable = PageRequest.of(0, size)
        )

        return withContext(Dispatchers.IO) { postRepository.getAllVotesExceptBlock(spec) }.content
    }
}

package com.oksusu.susu.api.post.application

import com.oksusu.susu.api.exception.ErrorCode
import com.oksusu.susu.api.exception.NotFoundException
import com.oksusu.susu.api.extension.withMDCContext
import com.oksusu.susu.api.post.domain.Post
import com.oksusu.susu.api.post.domain.vo.PostType
import com.oksusu.susu.api.post.infrastructure.repository.PostRepository
import com.oksusu.susu.api.post.infrastructure.repository.model.GetVoteSpec
import com.oksusu.susu.api.post.infrastructure.repository.model.PostAndUserModel
import com.oksusu.susu.api.post.infrastructure.repository.model.PostAndVoteCountModel
import com.oksusu.susu.api.post.infrastructure.repository.model.PostAndVoteOptionModel
import com.oksusu.susu.api.post.infrastructure.repository.model.SearchVoteSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class VoteService(
    private val postService: PostService,
    private val postRepository: PostRepository,
) {
    val logger = KotlinLogging.logger { }

    suspend fun getVoteAndCountExceptBlock(spec: GetVoteSpec): Slice<PostAndVoteCountModel> {
        return withMDCContext(Dispatchers.IO) {
            postRepository.getVoteAndCountExceptBlock(spec)
        }
    }

    suspend fun getVote(id: Long): Post {
        return postService.findByIdAndIsActiveAndTypeOrThrow(id, true, PostType.VOTE)
    }

    suspend fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel> {
        return withMDCContext(Dispatchers.IO) {
            postRepository.getVoteAndOptions(id)
        }.takeUnless { it.isEmpty() } ?: throw NotFoundException(ErrorCode.NOT_FOUND_VOTE_ERROR)
    }

    suspend fun getPopularVotesExceptBlock(
        uid: Long,
        userBlockIds: Set<Long>,
        postBlockIds: Set<Long>,
        size: Int,
    ): List<PostAndVoteCountModel> {
        val spec = GetVoteSpec(
            uid = uid,
            searchSpec = SearchVoteSpec.defaultPopularSpec(),
            userBlockIds = userBlockIds,
            postBlockIds = postBlockIds,
            pageable = PageRequest.of(0, size)
        )

        return withMDCContext(Dispatchers.IO) { postRepository.getVoteAndCountExceptBlock(spec) }.content
    }

    suspend fun getVoteAndCreator(id: Long): PostAndUserModel {
        return postService.getPostAndCreator(id, PostType.VOTE)
    }
}

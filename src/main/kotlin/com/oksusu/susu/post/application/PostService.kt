package com.oksusu.susu.post.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(
    private val postRepository: PostRepository,
) {
    @Transactional
    fun saveSync(post: Post): Post {
        return postRepository.save(post)
    }

    suspend fun findByIdOrThrow(id: Long): Post {
        return findByIdOrNull(id) ?: throw NotFoundException(ErrorCode.NOT_FOUND_POST_ERROR)
    }

    suspend fun findByIdOrNull(id: Long): Post? {
        return withContext(Dispatchers.IO) { postRepository.findByIdOrNull(id) }
    }

    suspend fun findByIdAndIsActiveAndTypeOrThrow(id: Long, isActive: Boolean, type: PostType): Post {
        return withContext(Dispatchers.IO) {
            postRepository.findByIdAndIsActiveAndType(id, true, PostType.VOTE)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_POST_ERROR)
    }

    suspend fun findByIsActiveAndTypeAndIdInExceptBlock(
        isActive: Boolean,
        type: PostType,
        ids: List<Long>,
        userBlockIds: List<Long>,
        postBlockIds: List<Long>,
    ): List<Post> {
        return withContext(Dispatchers.IO) {
            postRepository.findByIsActiveAndTypeAndIdInExceptBlock(
                isActive = isActive,
                type = type,
                ids = ids,
                userBlockIds = userBlockIds,
                postBlockIds = postBlockIds
            )
        }
    }

    suspend fun countAllByIsActiveAndType(isActive: Boolean, type: PostType): Long {
        return withContext(Dispatchers.IO) { postRepository.countAllByIsActiveAndType(isActive, type) }
    }

    suspend fun validateExist(id: Long) {
        withContext(Dispatchers.IO) {
            postRepository.existsById(id)
        }.takeIf { isExist -> isExist } ?: throw InvalidRequestException(ErrorCode.NOT_FOUND_POST_ERROR)
    }

    suspend fun validateAuthority(id: Long, uid: Long) {
        withContext(Dispatchers.IO) {
            findByIdOrThrow(id)
        }.takeIf { post -> post.uid == uid } ?: throw InvalidRequestException(ErrorCode.NO_AUTHORITY_ERROR)
    }

    suspend fun existsById(id: Long): Boolean {
        return withContext(Dispatchers.IO) { postRepository.existsById(id) }
    }
}

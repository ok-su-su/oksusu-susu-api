package com.oksusu.susu.api.post.application

import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.vo.PostType
import com.oksusu.susu.domain.post.infrastructure.repository.PostRepository
import com.oksusu.susu.domain.post.infrastructure.repository.model.PostAndUserModel
import kotlinx.coroutines.Dispatchers
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

    @Transactional
    fun saveAllSync(posts: List<Post>): List<Post> {
        return postRepository.saveAll(posts)
    }

    suspend fun findByIdOrThrow(id: Long): Post {
        return findByIdOrNull(id) ?: throw NotFoundException(ErrorCode.NOT_FOUND_POST_ERROR)
    }

    suspend fun findByIdOrNull(id: Long): Post? {
        return withMDCContext(Dispatchers.IO) { postRepository.findByIdOrNull(id) }
    }

    suspend fun findByIdAndIsActiveAndTypeOrThrow(id: Long, isActive: Boolean, type: PostType): Post {
        return withMDCContext(Dispatchers.IO) {
            postRepository.findByIdAndIsActiveAndType(id, true, PostType.VOTE)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_POST_ERROR)
    }

    suspend fun validateExist(id: Long) {
        withMDCContext(Dispatchers.IO) {
            postRepository.existsById(id)
        }.takeIf { isExist -> isExist } ?: throw InvalidRequestException(ErrorCode.NOT_FOUND_POST_ERROR)
    }

    suspend fun validateAuthority(id: Long, uid: Long) {
        withMDCContext(Dispatchers.IO) {
            findByIdOrThrow(id)
        }.takeIf { post -> post.uid == uid } ?: throw InvalidRequestException(ErrorCode.NO_AUTHORITY_ERROR)
    }

    suspend fun existsById(id: Long): Boolean {
        return withMDCContext(Dispatchers.IO) { postRepository.existsById(id) }
    }

    suspend fun findAllByUid(uid: Long): List<Post> {
        return withMDCContext(Dispatchers.IO) {
            postRepository.findAllByUid(uid)
        }
    }

    suspend fun getPostAndCreator(id: Long, type: PostType): PostAndUserModel {
        return withMDCContext(Dispatchers.IO) {
            postRepository.getPostAndCreator(id, type)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_POST_ERROR)
    }
}

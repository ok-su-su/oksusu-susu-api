package com.oksusu.susu.community.application

import com.oksusu.susu.community.domain.Post
import com.oksusu.susu.community.domain.vo.PostType
import com.oksusu.susu.community.infrastructure.repository.PostRepository
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
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

    suspend fun findAllByIsActiveAndTypeOrderByCreatedAtDes(
        isActive: Boolean,
        type: PostType,
        pageable: Pageable,
    ): Slice<Post> {
        return withContext(Dispatchers.IO) {
            postRepository.findAllByIsActiveAndTypeOrderByCreatedAtDesc(
                isActive,
                type,
                pageable
            )
        }
    }

    suspend fun findByIdOrThrow(id: Long): Post {
        return findByIdOrNull(id) ?: throw NotFoundException(ErrorCode.NOT_FOUND_COMMUNITY_ERROR)
    }

    suspend fun findByIdOrNull(id: Long): Post? {
        return withContext(Dispatchers.IO) {
            postRepository.findByIdOrNull(id)
        }
    }

    suspend fun findByIdAndIsActiveAndTypeOrThrow(id: Long, isActive: Boolean, type: PostType): Post {
        return withContext(Dispatchers.IO) {
            postRepository.findByIdAndIsActiveAndType(id, true, PostType.VOTE)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_COMMUNITY_ERROR)
    }

    suspend fun findByIsActiveAndTypeAndIdIn(isActive: Boolean, type: PostType, ids: List<Long>): List<Post> {
        return withContext(Dispatchers.IO) {
            postRepository.findByIsActiveAndTypeAndIdIn(isActive, type, ids)
        }
    }

    suspend fun countAllByIsActiveAndType(isActive: Boolean, type: PostType): Long {
        return withContext(Dispatchers.IO) {
            postRepository.countAllByIsActiveAndType(isActive, type)
        }
    }
}

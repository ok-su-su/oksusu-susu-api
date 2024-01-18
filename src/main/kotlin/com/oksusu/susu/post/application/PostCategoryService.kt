package com.oksusu.susu.post.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.resolveCancellation
import com.oksusu.susu.post.domain.PostCategory
import com.oksusu.susu.post.infrastructure.repository.PostCategoryRepository
import com.oksusu.susu.post.model.PostCategoryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class PostCategoryService(
    private val postCategoryRepository: PostCategoryRepository,
) {
    private val logger = mu.KotlinLogging.logger { }
    private var postCategories: Map<Long, PostCategoryModel> = emptyMap()

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-post-categories.initial-delay:0}"
    )
    fun refreshPostCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            logger.info { "start refresh postCategories" }

            postCategories = runCatching {
                findAllByIsActive(true)
                    .map { postCategory -> PostCategoryModel.from(postCategory) }
                    .associateBy { postCategory -> postCategory.id }
            }.onFailure { e ->
                logger.resolveCancellation("refreshPostCategories", e)
            }.getOrDefault(postCategories)

            logger.info { "finish refresh postCategories" }
        }
    }

    suspend fun getAll(): List<PostCategoryModel> {
        return postCategories.values.toList()
    }

    suspend fun findAllByIsActive(isActive: Boolean): List<PostCategory> {
        return withContext(Dispatchers.IO) { postCategoryRepository.findAllByIsActive(isActive) }
    }

    fun getCategory(id: Long): PostCategoryModel {
        return postCategories[id] ?: throw NotFoundException(ErrorCode.NOT_FOUND_POST_CATEGORY_ERROR)
    }

    fun getCategoryByIdIn(ids: List<Long>): Set<PostCategoryModel> {
        return ids.map { id -> getCategory(id) }.toSet()
    }

    fun validateExistCategory(postCategoryId: Long) {
        getCategory(postCategoryId)
    }
}

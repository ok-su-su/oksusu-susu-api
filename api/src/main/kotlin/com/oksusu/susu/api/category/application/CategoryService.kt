package com.oksusu.susu.api.category.application

import com.oksusu.susu.api.category.model.CategoryModel
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.resolveCancellation
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.category.domain.Category
import com.oksusu.susu.domain.category.infrastructure.CategoryRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    private val logger = KotlinLogging.logger { }
    private var categories: Map<Long, CategoryModel> = emptyMap()

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-categories.initial-delay:0}"
    )
    fun refreshCategories() {
        CoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler).launch {
            logger.info { "start refresh categories" }

            categories = runCatching {
                findAll()
                    .map { category -> CategoryModel.from(category) }
                    .sortedBy { category -> category.seq }
                    .associateBy { category -> category.id }
            }.onFailure { e ->
                logger.resolveCancellation("refreshCategories", e)
            }.getOrDefault(categories)

            logger.info { "finish refresh categories" }
        }
    }

    suspend fun getAllByActive(active: Boolean = true): List<CategoryModel> {
        return categories.values
            .filter { category -> category.isActive }
            .toList()
    }

    suspend fun findAllByIsActive(isActive: Boolean): List<Category> {
        return withMDCContext { categoryRepository.findAllByIsActive(isActive) }
    }

    suspend fun findAll(): List<Category> {
        return withContext(Dispatchers.IO) { categoryRepository.findAll() }
    }

    fun getCategory(id: Long): CategoryModel {
        return categories[id] ?: throw NotFoundException(ErrorCode.NOT_FOUND_CATEGORY_ERROR)
    }
}

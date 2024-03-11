package com.oksusu.susu.api.category.application

import com.oksusu.susu.domain.category.domain.Category
import com.oksusu.susu.domain.category.infrastructure.CategoryRepository
import com.oksusu.susu.api.category.model.CategoryModel
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.resolveCancellation
import com.oksusu.susu.common.extension.withMDCContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
    private val logger = KotlinLogging.logger { }
    private var categories: Map<Long, CategoryModel> = emptyMap()

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-categories.initial-delay:0}"
    )
    fun refreshCategories() {
        CoroutineScope(Dispatchers.IO + Job()).launch {
            logger.info { "start refresh categories" }

            categories = runCatching {
                findAllByIsActive(true)
                    .map { category -> CategoryModel.from(category) }
                    .associateBy { category -> category.id }
            }.onFailure { e ->
                logger.resolveCancellation("refreshCategories", e)
            }.getOrDefault(categories)

            logger.info { "finish refresh categories" }
        }
    }

    suspend fun getAll(): List<CategoryModel> {
        return categories.values.toList()
    }

    suspend fun findAllByIsActive(isActive: Boolean): List<Category> {
        return withMDCContext { categoryRepository.findAllByIsActive(isActive) }
    }

    fun getCategory(id: Long): CategoryModel {
        return categories[id] ?: throw NotFoundException(ErrorCode.NOT_FOUND_CATEGORY_ERROR)
    }
}

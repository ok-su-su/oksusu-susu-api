package com.oksusu.susu.api.category.application

import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.category.domain.CategoryAssignment
import com.oksusu.susu.domain.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.domain.category.infrastructure.CategoryAssignmentRepository
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryAssignmentService(
    private val categoryAssignmentRepository: CategoryAssignmentRepository,
) {
    @Transactional
    fun saveSync(categoryAssignment: CategoryAssignment): CategoryAssignment {
        return categoryAssignmentRepository.save(categoryAssignment)
    }

    @Transactional
    fun deleteByTargetIdAndTargetTypeSync(targetId: Long, targetType: CategoryAssignmentType) {
        categoryAssignmentRepository.deleteByTargetIdAndTargetType(targetId, targetType)
    }

    @Transactional
    fun deleteAllByTargetTypeAndTargetIdIn(targetType: CategoryAssignmentType, targetIds: List<Long>) {
        categoryAssignmentRepository.deleteAllByTargetTypeAndTargetIdIn(targetType, targetIds)
    }

    suspend fun findByIdAndTypeOrNull(targetId: Long, targetType: CategoryAssignmentType): CategoryAssignment? {
        return withMDCContext(Dispatchers.IO) {
            categoryAssignmentRepository.findByTargetIdAndTargetType(targetId, targetType)
        }
    }

    suspend fun findAllByTypeAndIdIn(
        targetType: CategoryAssignmentType,
        targetIds: List<Long>,
    ): List<CategoryAssignment> {
        return withMDCContext(Dispatchers.IO) {
            categoryAssignmentRepository.findAllByTargetTypeAndTargetIdIn(targetType, targetIds)
        }
    }
}

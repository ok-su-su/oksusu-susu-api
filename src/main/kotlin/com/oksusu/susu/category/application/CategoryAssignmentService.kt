package com.oksusu.susu.category.application

import com.oksusu.susu.category.domain.CategoryAssignment
import com.oksusu.susu.category.infrastructure.CategoryAssignmentRepository
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
}

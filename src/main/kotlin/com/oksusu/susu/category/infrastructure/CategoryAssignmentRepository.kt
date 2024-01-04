package com.oksusu.susu.category.infrastructure

import com.oksusu.susu.category.domain.CategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CategoryAssignmentRepository : JpaRepository<CategoryAssignment, Long> {
    @Transactional(readOnly = true)
    fun findByTargetIdAndTargetType(targetId: Long, targetType: CategoryAssignmentType): CategoryAssignment

    @Transactional(readOnly = true)
    fun findAllByTargetTypeAndTargetIdIn(
        targetType: CategoryAssignmentType,
        targetIds: List<Long>,
    ): List<CategoryAssignment>

    @Transactional
    fun deleteByTargetIdAndTargetType(targetId: Long, targetType: CategoryAssignmentType)

    @Transactional
    fun deleteAllByTargetTypeAndTargetIdIn(targetType: CategoryAssignmentType, targetIds: List<Long>)
}

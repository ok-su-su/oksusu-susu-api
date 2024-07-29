package com.oksusu.susu.domain.category.infrastructure

import com.oksusu.susu.domain.category.domain.CategoryAssignment
import com.oksusu.susu.domain.category.domain.vo.CategoryAssignmentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface CategoryAssignmentRepository : JpaRepository<CategoryAssignment, Long> {
    fun findByTargetIdAndTargetType(targetId: Long, targetType: CategoryAssignmentType): CategoryAssignment

    fun findAllByTargetTypeAndTargetIdIn(
        targetType: CategoryAssignmentType,
        targetIds: List<Long>,
    ): List<CategoryAssignment>

    fun deleteByTargetIdAndTargetType(targetId: Long, targetType: CategoryAssignmentType)

    fun deleteAllByTargetTypeAndTargetIdIn(targetType: CategoryAssignmentType, targetIds: List<Long>)
}

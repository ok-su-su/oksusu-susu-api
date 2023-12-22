package com.oksusu.susu.category.infrastructure

import com.oksusu.susu.category.domain.CategoryAssignment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryAssignmentRepository : JpaRepository<CategoryAssignment, Long>

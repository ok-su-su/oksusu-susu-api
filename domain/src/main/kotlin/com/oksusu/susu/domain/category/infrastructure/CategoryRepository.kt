package com.oksusu.susu.domain.category.infrastructure

import com.oksusu.susu.domain.category.domain.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findAllByIsActive(isActive: Boolean): List<Category>
}

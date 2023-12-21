package com.oksusu.susu.category.infrastructure

import com.oksusu.susu.category.domain.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    @Transactional(readOnly = true)
    fun findAllByIsActive(isActive: Boolean): List<Category>
}

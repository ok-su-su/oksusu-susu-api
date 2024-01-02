package com.oksusu.susu.post.infrastructure.repository

import com.oksusu.susu.post.domain.PostCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PostCategoryRepository : JpaRepository<PostCategory, Long> {
    @Transactional(readOnly = true)
    fun findAllByIsActive(active: Boolean): List<PostCategory>
}

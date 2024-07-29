package com.oksusu.susu.domain.post.infrastructure.repository

import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.vo.PostType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface PostRepository : JpaRepository<Post, Long>, PostQRepository {
    fun findByIdAndIsActiveAndType(id: Long, isActive: Boolean, type: PostType): Post?

    fun findAllByUid(uid: Long): List<Post>

    fun findAllByUidIn(uid: List<Long>): List<Post>

    fun findAllByIdIn(punishPostIds: List<Long>): List<Post>
}

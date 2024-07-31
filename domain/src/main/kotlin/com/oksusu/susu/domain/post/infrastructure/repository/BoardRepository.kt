package com.oksusu.susu.domain.post.infrastructure.repository

import com.oksusu.susu.domain.post.domain.Board
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface BoardRepository : JpaRepository<Board, Long> {
    fun findAllByIsActive(active: Boolean): List<Board>
}

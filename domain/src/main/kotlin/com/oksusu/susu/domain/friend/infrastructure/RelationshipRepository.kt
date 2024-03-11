package com.oksusu.susu.domain.friend.infrastructure

import com.oksusu.susu.domain.friend.domain.Relationship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface RelationshipRepository : JpaRepository<Relationship, Long> {
    @Transactional(readOnly = true)
    fun findAllByIsActive(isActive: Boolean): List<Relationship>
}

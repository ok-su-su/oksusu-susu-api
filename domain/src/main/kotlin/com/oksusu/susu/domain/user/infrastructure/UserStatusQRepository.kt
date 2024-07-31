package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.QUserStatus
import com.oksusu.susu.domain.user.domain.UserStatus
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
interface UserStatusQRepository {
    fun updateAllCreatedAt(createdAt: LocalDateTime): Long
}

class UserStatusQRepositoryImpl : UserStatusQRepository, QuerydslRepositorySupport(UserStatus::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qUserStatus = QUserStatus.userStatus

    override fun updateAllCreatedAt(createdAt: LocalDateTime): Long {
        return JPAQueryFactory(entityManager)
            .update(qUserStatus)
            .set(qUserStatus.createdAt, createdAt)
            .execute()
    }
}

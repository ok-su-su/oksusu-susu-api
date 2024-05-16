package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.common.extension.isEquals
import com.oksusu.susu.domain.user.domain.QUser
import com.oksusu.susu.domain.user.domain.QUserStatus
import com.oksusu.susu.domain.user.domain.User
import com.oksusu.susu.domain.user.domain.vo.OauthInfo
import com.oksusu.susu.domain.user.infrastructure.model.QUserAndUserStatusModel
import com.oksusu.susu.domain.user.infrastructure.model.UserAndUserStatusModel
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface UserRepository : JpaRepository<User, Long>, UserCustomRepository {
    @Transactional(readOnly = true)
    fun existsByOauthInfo(oauthInfo: OauthInfo): Boolean

    @Transactional(readOnly = true)
    fun findByOauthInfo(oauthInfo: OauthInfo): User?

    @Transactional(readOnly = true)
    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long
}

interface UserCustomRepository {
    @Transactional(readOnly = true)
    fun getUserAndUserStatus(uid: Long): UserAndUserStatusModel?
}

class UserCustomRepositoryImpl : UserCustomRepository, QuerydslRepositorySupport(User::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qUser = QUser.user
    private val qUserStatus = QUserStatus.userStatus

    override fun getUserAndUserStatus(uid: Long): UserAndUserStatusModel? {
        return JPAQuery<QUser>(entityManager)
            .select(QUserAndUserStatusModel(qUser, qUserStatus))
            .from(qUser)
            .join(qUserStatus).on(qUser.id.eq(qUserStatus.uid))
            .where(qUser.id.isEquals(uid))
            .fetchFirst()
    }
}

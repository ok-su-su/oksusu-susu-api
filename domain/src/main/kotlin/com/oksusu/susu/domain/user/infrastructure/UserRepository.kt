package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.User
import com.oksusu.susu.domain.user.domain.vo.OauthInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
@Repository
interface UserRepository : JpaRepository<User, Long>, UserQRepository {
    fun existsByOauthInfo(oauthInfo: OauthInfo): Boolean

    fun findByOauthInfo(oauthInfo: OauthInfo): User?

    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long
}

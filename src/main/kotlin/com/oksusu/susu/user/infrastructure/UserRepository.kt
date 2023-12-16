package com.oksusu.susu.user.infrastructure

import com.oksusu.susu.user.domain.OauthInfo
import com.oksusu.susu.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Transactional(readOnly = true)
    fun existsByOauthInfo(oauthInfo: OauthInfo): Boolean

    @Transactional(readOnly = true)
    fun findByOauthInfo(oauthInfo: OauthInfo): User?
}

package com.oksusu.susu.user.infrastructure

import com.oksusu.susu.user.domain.OauthInfo
import com.oksusu.susu.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByOauthInfo(oauthInfo: OauthInfo): Boolean
}

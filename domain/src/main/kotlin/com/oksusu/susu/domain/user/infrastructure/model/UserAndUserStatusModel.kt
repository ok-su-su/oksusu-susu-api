package com.oksusu.susu.domain.user.infrastructure.model

import com.oksusu.susu.domain.user.domain.User
import com.oksusu.susu.domain.user.domain.UserStatus
import com.querydsl.core.annotations.QueryProjection

class UserAndUserStatusModel @QueryProjection constructor(
    val user: User,
    val userStatus: UserStatus,
)

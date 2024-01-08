package com.oksusu.susu.user.model.response

import com.oksusu.susu.common.annotation.DateFormat
import com.oksusu.susu.user.domain.User
import com.oksusu.susu.user.domain.vo.Gender
import java.time.LocalDate

class UserInfoResponse(
    val id: Long,
    val name: String,
    val gender: Gender?,
    @DateFormat
    val birth: LocalDate?,
) {
    companion object {
        fun from(user: User): UserInfoResponse {
            return UserInfoResponse(
                id = user.id,
                name = user.name,
                gender = user.gender,
                birth = user.birth
            )
        }
    }
}

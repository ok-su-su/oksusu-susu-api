package com.oksusu.susu.api.user.model.response

import com.oksusu.susu.api.user.domain.User
import com.oksusu.susu.api.user.domain.vo.Gender

class UserInfoResponse(
    /** 유저 id */
    val id: Long,
    /** 이름 */
    val name: String,
    /** 성별 */
    val gender: Gender?,
    /** 출생년도 */
    val birth: Int?,
) {
    companion object {
        fun from(user: User): UserInfoResponse {
            return UserInfoResponse(
                id = user.id,
                name = user.name,
                gender = user.gender,
                birth = user.birth?.year
            )
        }
    }
}

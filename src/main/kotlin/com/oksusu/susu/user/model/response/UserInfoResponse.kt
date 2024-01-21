package com.oksusu.susu.user.model.response

import com.oksusu.susu.user.domain.User
import com.oksusu.susu.user.domain.vo.Gender

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

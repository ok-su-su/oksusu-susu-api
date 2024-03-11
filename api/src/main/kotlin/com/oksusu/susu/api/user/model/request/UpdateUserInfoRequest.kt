package com.oksusu.susu.api.user.model.request

import com.oksusu.susu.domain.user.domain.vo.Gender
import java.time.LocalDate

data class UpdateUserInfoRequest(
    /** 이름 */
    val name: String,
    /** 성별 */
    val gender: Gender?,
    /** 출생년도 */
    val birth: Int?,
) {
    fun getBirth(): LocalDate? {
        return this.birth?.let { LocalDate.of(it, 1, 1) }
    }
}

package com.oksusu.susu.user.model.request

import com.oksusu.susu.user.domain.vo.Gender
import java.time.LocalDate

data class UpdateUserInfoRequest(
    val name: String,
    val gender: Gender?,
    val birth: Int?,
) {
    fun getBirth(): LocalDate? {
        return this.birth?.let { LocalDate.of(it, 1, 1) }
    }
}

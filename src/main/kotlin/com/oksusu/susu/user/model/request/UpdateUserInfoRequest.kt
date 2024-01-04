package com.oksusu.susu.user.model.request

import com.oksusu.susu.user.domain.Gender
import java.time.LocalDate

class UpdateUserInfoRequest(
    val name: String,
    val gender: Gender?,
    val birth: Int?,
) {
    fun getBirth(): LocalDate? {
        return this.birth ?.let { LocalDate.of(it, 1, 1) }
    }
}
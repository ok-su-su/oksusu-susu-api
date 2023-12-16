package com.oksusu.susu.auth.model.dto.request

import java.time.LocalDate

class OauthRegisterRequest(
    val name: String,
    val age: Int?,
    val birth: Int?,
) {
    fun getBirth(): LocalDate? {
        return this.birth ?.let { LocalDate.of(it, 1, 1) }
    }
}

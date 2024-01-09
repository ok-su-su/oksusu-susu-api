package com.oksusu.susu.auth.model.dto.request

import com.oksusu.susu.user.domain.vo.Gender
import java.time.LocalDate

class OauthRegisterRequest(
    val name: String,
    val termAgreement: List<Long>,
    val gender: Gender?,
    val birth: Int?,
) {
    fun getBirth(): LocalDate? {
        return this.birth ?.let { LocalDate.of(it, 1, 1) }
    }
}

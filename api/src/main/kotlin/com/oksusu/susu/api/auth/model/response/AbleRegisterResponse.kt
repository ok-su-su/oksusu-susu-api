package com.oksusu.susu.api.auth.model.response

data class AbleRegisterResponse(
    /** 회원가입 가능 여부 / 가능 : 1, 불가능 : 0 */
    val canRegister: Boolean,
)

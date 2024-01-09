package com.oksusu.susu.auth.model

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NoAuthorityException

interface AuthUser {
    val id: Long

    fun isAuthor(uid: Long): Boolean

    fun isAuthorThrow(uid: Long)
}

class AuthUserImpl(
    override val id: Long,
) : AuthUser {
    override fun isAuthor(uid: Long): Boolean {
        return this.id == uid
    }

    override fun isAuthorThrow(uid: Long) {
        if (isAuthor(uid)) {
            throw NoAuthorityException(ErrorCode.NO_AUTHORITY_ERROR)
        }
    }
}

const val AUTH_TOKEN_KEY = "X-SUSU-AUTH-TOKEN"

data class AuthUserToken(
    val key: String,
    val value: String,
) {
    fun isInvalid() = key.isBlank() || value.isBlank()

    companion object {
        fun from(value: String): AuthUserToken {
            return AuthUserToken(
                key = AUTH_TOKEN_KEY,
                value = value
            )
        }
    }
}

data class AuthUserTokenPayload(
    val id: Long,
    val aud: String,
    val iss: String,
    val exp: Long,
    val type: String,
)

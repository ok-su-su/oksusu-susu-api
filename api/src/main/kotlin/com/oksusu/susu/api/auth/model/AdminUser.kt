package com.oksusu.susu.api.auth.model

import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NoAuthorityException

/**
 * 어드민 유저용 argument 입니다.
 */
interface AdminUser {
    /** user id */
    val uid: Long

    fun isAuthor(uid: Long): Boolean

    fun isAuthorThrow(uid: Long)

    fun isNotAuthorThrow(uid: Long)
}

class AdminUserImpl(
    override val uid: Long,
) : AdminUser {
    override fun isAuthor(uid: Long): Boolean {
        return this.uid == uid
    }

    override fun isAuthorThrow(uid: Long) {
        if (isAuthor(uid)) {
            throw NoAuthorityException(ErrorCode.NO_AUTHORITY_ERROR)
        }
    }

    override fun isNotAuthorThrow(uid: Long) {
        if (!isAuthor(uid)) {
            throw NoAuthorityException(ErrorCode.NO_AUTHORITY_ERROR)
        }
    }
}

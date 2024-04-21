package com.oksusu.susu.domain.user.domain.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

/** oauth 정보 */
@Embeddable
data class OauthInfo(
    /** oauth provider */
    @Column(name = "oauth_provider")
    @Enumerated(EnumType.ORDINAL)
    val oAuthProvider: OAuthProvider,

    /** oauth id */
    @Column(name = "oauth_id")
    val oAuthId: String,
) {
    fun withdrawOAuthInfo(): OauthInfo {
        return OauthInfo(this.oAuthProvider, withDrawOid())
    }

    private fun withDrawOid(now: LocalDateTime = LocalDateTime.now()): String {
        return "withdraw:$now:${this.oAuthId}"
    }
}

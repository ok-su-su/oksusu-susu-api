package com.oksusu.susu.user.domain

import com.oksusu.susu.auth.model.OauthProvider
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

@Embeddable
data class OauthInfo(
    @Column(name = "oauth_provider")
    @Enumerated(EnumType.ORDINAL)
    val oauthProvider: OauthProvider,

    @Column(name = "oauth_id")
    val oauthId: String,
) {
    fun withdrawOauthInfo(): OauthInfo {
        return OauthInfo(this.oauthProvider, withDrawOid())
    }

    private fun withDrawOid(now: LocalDateTime = LocalDateTime.now()): String {
        return "withdraw:$now:${this.oauthId}"
    }
}

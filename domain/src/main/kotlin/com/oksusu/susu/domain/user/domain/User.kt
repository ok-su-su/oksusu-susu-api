package com.oksusu.susu.domain.user.domain

import com.oksusu.susu.domain.common.BaseEntity
import com.oksusu.susu.domain.user.domain.vo.AccountRole
import com.oksusu.susu.domain.user.domain.vo.Gender
import com.oksusu.susu.domain.user.domain.vo.OauthInfo
import jakarta.persistence.*
import java.time.LocalDate

/** 유저 */
@Entity
@Table(name = "user")
class User(
    /** user id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** oauth 정보 */
    @Embedded
    var oauthInfo: OauthInfo,

    /** 이름 */
    var name: String,

    /** 성별 */
    @Enumerated(EnumType.ORDINAL)
    var gender: Gender? = null,

    /** 생년월일 */
    var birth: LocalDate? = null,

    /** 프로필 이미지 */
    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    /**
     * 계정 권한
     */
    @Enumerated(EnumType.STRING)
    val role: AccountRole,

    // TODO : 아래의 필드들은 apple 인증 마이그레이션을 위한 코드들
    /** apple 인증 처리를 위한 임시 컬럼 */
    @Column(name = "transfer_oauth_id")
    val transferOAuthId: String? = null, // TODO: 추후 제거 필요, 마이그를 위한 임시 코드

    @Column(name = "new_oauth_id")
    val newOAuthId: String? = null, // TODO: 추후 제거 필요, 마이그를 위한 임시 코드

    @Column(name = "new_email")
    val newEmail: String? = null, // TODO: 추후 제거 필요, 마이그를 위한 임시 코드

    @Column(name = "old_sub")
    val oldSub: String? = null,
) : BaseEntity() {
    override fun toString(): String {
        return "User(id=$id, oauthInfo=$oauthInfo, name='$name', gender=$gender, birth=$birth, profileImageUrl=$profileImageUrl, role=$role)"
    }
}

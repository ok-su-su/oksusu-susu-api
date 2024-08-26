package com.oksusu.susu.domain.metadata.domain

import com.oksusu.susu.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "application_metadata")
class ApplicationMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** ios 최소 지원 어플리케이션 버전 */
    @Column(name = "ios_min_support_version")
    val iosMinSupportVersion: String,

    /** aos 최소 지원 어플리케이션 버전 */
    @Column(name = "aos_min_support_version")
    val aosMinSupportVersion: String,

    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()

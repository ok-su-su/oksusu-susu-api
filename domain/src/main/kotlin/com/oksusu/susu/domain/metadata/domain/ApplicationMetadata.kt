package com.oksusu.susu.domain.metadata.domain

import com.oksusu.susu.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "application_metadata")
class ApplicationMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 최신 어플리케이션 버전 */
    @Column(name = "application_version")
    val applicationVersion: String,

    /** 강제 업데이트 날짜 */
    @Column(name = "forced_update_date")
    val forcedUpdateDate: LocalDateTime,

    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()

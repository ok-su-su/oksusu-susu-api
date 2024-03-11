package com.oksusu.susu.api.log.domain

import com.oksusu.susu.api.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "system_action_log")
class SystemActionLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1L,

    @Column(name = "ip_address")
    val ipAddress: String? = null,

    val path: String? = null,

    @Column(name = "http_method")
    val httpMethod: String? = null,

    @Column(name = "user_agent")
    val userAgent: String? = null,

    val host: String? = null,

    val referer: String? = null,

    @Column(columnDefinition = "TEXT")
    val extra: String? = null,
) : BaseEntity()

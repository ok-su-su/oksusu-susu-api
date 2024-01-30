package com.oksusu.susu.friend.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/** 지인 */
@Entity
@Table(name = "friend")
class Friend(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** user id */
    val uid: Long,

    /** 지인 이름 */
    var name: String,

    /** 전화번호 */
    @Column(name = "phone_number")
    var phoneNumber: String? = null,
) : BaseEntity()

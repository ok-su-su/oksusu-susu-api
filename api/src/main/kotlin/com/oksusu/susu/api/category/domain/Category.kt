package com.oksusu.susu.api.category.domain

import com.oksusu.susu.api.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "category")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val seq: Long,

    val name: String,

    val style: String,

    @Column(name = "is_active")
    val isActive: Boolean,
) : BaseEntity()

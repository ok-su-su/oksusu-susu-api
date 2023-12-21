package com.oksusu.susu.category.domain

import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "category_assignment")
class CategoryAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(name = "target_id")
    val targetId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    val targetType: CategoryAssignmentType,

    @Column(name = "custom_category")
    val customCategory: String? = null,
) : BaseEntity()

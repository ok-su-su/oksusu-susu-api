package com.oksusu.susu.api.category.domain

import com.oksusu.susu.api.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.api.domain.BaseEntity
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

    @Column(name = "category_id")
    var categoryId: Long,

    @Column(name = "custom_category")
    var customCategory: String? = null,
) : BaseEntity()

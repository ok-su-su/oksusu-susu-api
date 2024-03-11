package com.oksusu.susu.domain.category.domain

import com.oksusu.susu.domain.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.domain.common.BaseEntity
import jakarta.persistence.*

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

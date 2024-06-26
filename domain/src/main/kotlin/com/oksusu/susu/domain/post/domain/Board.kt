package com.oksusu.susu.domain.post.domain

import com.oksusu.susu.domain.common.BaseEntity
import jakarta.persistence.*

/** 보드 */
@Entity
@Table(name = "board")
class Board(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 카테고리 명 */
    val name: String,

    /** 카테고리 순서 */
    val seq: Int,

    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity() {
    override fun toString(): String {
        return "Board(id=$id, name='$name', seq=$seq, isActive=$isActive)"
    }
}

package com.oksusu.susu.api.user.infrastructure

import com.oksusu.susu.api.user.domain.UserBlock
import com.oksusu.susu.api.user.domain.vo.UserBlockTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserBlockRepository : JpaRepository<UserBlock, Long> {
    @Transactional(readOnly = true)
    fun existsByUidAndTargetIdAndTargetType(uid: Long, targetId: Long, targetType: UserBlockTargetType): Boolean

    @Transactional(readOnly = true)
    fun findAllByUid(uid: Long): List<UserBlock>

    @Transactional(readOnly = true)
    fun findByTargetIdAndTargetType(targetId: Long, targetType: UserBlockTargetType): UserBlock?
}

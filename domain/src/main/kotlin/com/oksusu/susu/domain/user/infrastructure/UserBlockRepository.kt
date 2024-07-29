package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.UserBlock
import com.oksusu.susu.domain.user.domain.vo.UserBlockTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface UserBlockRepository : JpaRepository<UserBlock, Long> {
    fun existsByUidAndTargetIdAndTargetType(uid: Long, targetId: Long, targetType: UserBlockTargetType): Boolean

    fun findAllByUid(uid: Long): List<UserBlock>

    fun findByTargetIdAndTargetType(targetId: Long, targetType: UserBlockTargetType): UserBlock?
}

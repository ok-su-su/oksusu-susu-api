package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.UserStatusHistory
import com.oksusu.susu.domain.user.domain.vo.UserStatusAssignmentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface UserStatusHistoryRepository : JpaRepository<UserStatusHistory, Long>, UserStatusHistoryQRepository {
    fun findAllByIsForcedAndStatusAssignmentTypeAndToStatusId(
        isForced: Boolean,
        assignmentType: UserStatusAssignmentType,
        toStatusId: Long,
    ): List<UserStatusHistory>
}

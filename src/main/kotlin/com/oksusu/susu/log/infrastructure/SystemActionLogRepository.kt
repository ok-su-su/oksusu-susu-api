package com.oksusu.susu.log.infrastructure

import com.oksusu.susu.log.domain.SystemActionLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SystemActionLogRepository : JpaRepository<SystemActionLog, Long>

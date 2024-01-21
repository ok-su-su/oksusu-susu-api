package com.oksusu.susu.count.infrastructure

import com.oksusu.susu.count.domain.Count
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CountRepository : JpaRepository<Count, Long>
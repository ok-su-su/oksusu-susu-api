package com.oksusu.susu.api.metadata.infrastructure

import com.oksusu.susu.api.metadata.domain.ApplicationMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ApplicationMetadataRepository : JpaRepository<ApplicationMetadata, Long> {
    @Transactional(readOnly = true)
    fun findTop1ByIsActiveOrderByCreatedAtDesc(isActive: Boolean): ApplicationMetadata
}

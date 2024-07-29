package com.oksusu.susu.domain.metadata.infrastructure

import com.oksusu.susu.domain.metadata.domain.ApplicationMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface ApplicationMetadataRepository : JpaRepository<ApplicationMetadata, Long> {
    fun findTop1ByIsActiveOrderByCreatedAtDesc(isActive: Boolean): ApplicationMetadata?
}

package com.oksusu.susu.friend.infrastructure

import com.oksusu.susu.friend.domain.Relationship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RelationshipRepository : JpaRepository<Relationship, Long>

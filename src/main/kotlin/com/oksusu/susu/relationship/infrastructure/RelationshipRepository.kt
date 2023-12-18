package com.oksusu.susu.relationship.infrastructure

import com.oksusu.susu.relationship.domain.Relationship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RelationshipRepository : JpaRepository<Relationship, Long>

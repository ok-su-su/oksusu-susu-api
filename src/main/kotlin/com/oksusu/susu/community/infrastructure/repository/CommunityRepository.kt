package com.oksusu.susu.community.infrastructure.repository

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.QCommunity
import com.oksusu.susu.community.domain.vo.CommunityType
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
interface CommunityRepository : JpaRepository<Community, Long>, CommunityCustomRepository {
    fun findAllByIsActiveAndTypeOrderByCreatedAtDesc(
        b: Boolean,
        vote: CommunityType,
        toDefault: Pageable
    ): Slice<Community>

    fun findByIdAndIsActiveAndType(id: Long, b: Boolean, vote: CommunityType): Community?

}

interface CommunityCustomRepository {
}

class CommunityCustomRepositoryImpl : CommunityCustomRepository, QuerydslRepositorySupport(Community::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qCommunity = QCommunity.community
}

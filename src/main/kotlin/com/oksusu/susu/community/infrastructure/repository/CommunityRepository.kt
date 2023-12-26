package com.oksusu.susu.community.infrastructure.repository

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.QCommunity
import com.oksusu.susu.community.domain.QVoteOption
import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.domain.vo.CommunityType
import com.oksusu.susu.community.infrastructure.repository.model.CommunityAndVoteOptionModel
import com.oksusu.susu.community.infrastructure.repository.model.QCommunityAndVoteOptionModel
import com.oksusu.susu.extension.executeSlice
import com.oksusu.susu.friend.domain.QFriend
import com.querydsl.jpa.impl.JPAQuery
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
        isActive: Boolean,
        type: CommunityType,
        toDefault: Pageable,
    ): Slice<Community>

    fun findByIdAndIsActiveAndType(id: Long, isActive: Boolean, type: CommunityType): Community?
    fun findByIsActiveAndTypeAndIdIn(isActive: Boolean, type: CommunityType, ids: List<Long>): List<Community>
    fun countAllByIsActiveAndType(isActive: Boolean, type: CommunityType): Long
}

interface CommunityCustomRepository {
    fun getVoteAndOptions(id: Long): List<CommunityAndVoteOptionModel>
    fun getAllVotes(
        isMine: Boolean,
        uid: Long,
        category: CommunityCategory,
        pageable: Pageable,
    ): Slice<Community>

    fun getAllVotesOrderByPopular(
        isMine: Boolean,
        uid: Long,
        category: CommunityCategory,
        ids: List<Long>
    ): List<Community>
}

class CommunityCustomRepositoryImpl : CommunityCustomRepository, QuerydslRepositorySupport(Community::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qCommunity = QCommunity.community
    private val qVoteOption = QVoteOption.voteOption

    override fun getVoteAndOptions(id: Long): List<CommunityAndVoteOptionModel> {
        return JPAQuery<QCommunity>(entityManager)
            .select(QCommunityAndVoteOptionModel(qCommunity, qVoteOption))
            .from(qCommunity)
            .leftJoin(qVoteOption).on(qCommunity.id.eq(qVoteOption.communityId))
            .where(
                qCommunity.id.eq(id),
                qCommunity.isActive.eq(true),
                qCommunity.type.eq(CommunityType.VOTE),
            ).fetch()
    }

    override fun getAllVotes(
        isMine: Boolean,
        uid: Long,
        category: CommunityCategory,
        pageable: Pageable,
    ): Slice<Community> {
        val uidFilter = qCommunity.uid.eq(uid).takeIf { isMine }
        val categoryFilter = qCommunity.category.eq(category).takeIf { category != CommunityCategory.ALL }

        val query = JPAQuery<QFriend>(entityManager)
            .select(qCommunity)
            .from(qCommunity)
            .where(
                qCommunity.isActive.eq(true),
                uidFilter,
                categoryFilter,
            ).orderBy(
                qCommunity.createdAt.desc()
            )

        return querydsl.executeSlice(query, pageable)
    }

    override fun getAllVotesOrderByPopular(
        isMine: Boolean,
        uid: Long,
        category: CommunityCategory,
        ids: List<Long>,
    ): List<Community> {
        val uidFilter = qCommunity.uid.eq(uid).takeIf { isMine }
        val categoryFilter = qCommunity.category.eq(category).takeIf { category != CommunityCategory.ALL }

        return JPAQuery<QCommunity>(entityManager)
            .select(qCommunity)
            .from(qCommunity)
            .where(
                qCommunity.isActive.eq(true),
                qCommunity.id.`in`(ids),
                uidFilter,
                categoryFilter,
            )
            .fetch()
    }
}

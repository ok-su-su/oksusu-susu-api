package com.oksusu.susu.community.infrastructure.repository

import com.oksusu.susu.category.domain.QCategoryAssignment
import com.oksusu.susu.common.consts.DEFAULT_CATEGORY_ID
import com.oksusu.susu.community.domain.Post
import com.oksusu.susu.community.domain.QPost
import com.oksusu.susu.community.domain.QVoteOption
import com.oksusu.susu.community.domain.vo.PostType
import com.oksusu.susu.community.infrastructure.repository.model.PostAndVoteOptionModel
import com.oksusu.susu.community.infrastructure.repository.model.QPostAndVoteOptionModel
import com.oksusu.susu.extension.executeSlice
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PostRepository : JpaRepository<Post, Long>, PostCustomRepository {
    @Transactional(readOnly = true)
    fun findAllByIsActiveAndTypeOrderByCreatedAtDesc(
        isActive: Boolean,
        type: PostType,
        toDefault: Pageable,
    ): Slice<Post>

    @Transactional(readOnly = true)
    fun findByIdAndIsActiveAndType(id: Long, isActive: Boolean, type: PostType): Post?

    @Transactional(readOnly = true)
    fun findByIsActiveAndTypeAndIdIn(isActive: Boolean, type: PostType, ids: List<Long>): List<Post>

    @Transactional(readOnly = true)
    fun countAllByIsActiveAndType(isActive: Boolean, type: PostType): Long
}

interface PostCustomRepository {
    @Transactional(readOnly = true)
    fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel>

    @Transactional(readOnly = true)
    fun getAllVotes(
        isMine: Boolean,
        uid: Long,
        categoryId: Long,
        pageable: Pageable,
    ): Slice<Post>

    @Transactional(readOnly = true)
    fun getAllVotesOrderByPopular(
        isMine: Boolean,
        uid: Long,
        categoryId: Long,
        ids: List<Long>,
    ): List<Post>
}

class PostCustomRepositoryImpl : PostCustomRepository, QuerydslRepositorySupport(Post::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qPost = QPost.post
    private val qVoteOption = QVoteOption.voteOption
    private val qCategoryAssignment = QCategoryAssignment.categoryAssignment

    override fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel> {
        return JPAQuery<QPost>(entityManager)
            .select(QPostAndVoteOptionModel(qPost, qVoteOption))
            .from(qPost)
            .leftJoin(qVoteOption).on(qPost.id.eq(qVoteOption.communityId))
            .where(
                qPost.id.eq(id),
                qPost.isActive.eq(true),
                qPost.type.eq(PostType.VOTE)
            ).fetch()
    }

    override fun getAllVotes(
        isMine: Boolean,
        uid: Long,
        categoryId: Long,
        pageable: Pageable,
    ): Slice<Post> {
        val uidFilter = qPost.uid.eq(uid).takeIf { isMine }
        val categoryFilter = qCategoryAssignment.categoryId.eq(categoryId).takeIf { categoryId != DEFAULT_CATEGORY_ID }

        val query = JPAQuery<QPost>(entityManager)
            .select(qPost)
            .from(qPost)
            .leftJoin(qCategoryAssignment).on(qPost.id.eq(qCategoryAssignment.targetId))
            .where(
                qPost.isActive.eq(true),
                uidFilter,
                categoryFilter
            ).orderBy(
                qPost.createdAt.desc()
            )

        return querydsl.executeSlice(query, pageable)
    }

    override fun getAllVotesOrderByPopular(
        isMine: Boolean,
        uid: Long,
        categoryId: Long,
        ids: List<Long>,
    ): List<Post> {
        val uidFilter = qPost.uid.eq(uid).takeIf { isMine }
        val categoryFilter = qCategoryAssignment.categoryId.eq(categoryId).takeIf { categoryId != DEFAULT_CATEGORY_ID }

        return JPAQuery<QPost>(entityManager)
            .select(qPost)
            .from(qPost)
            .leftJoin(qCategoryAssignment).on(qPost.id.eq(qCategoryAssignment.targetId))
            .where(
                qPost.isActive.eq(true),
                qPost.id.`in`(ids),
                uidFilter,
                categoryFilter
            )
            .fetch()
    }
}

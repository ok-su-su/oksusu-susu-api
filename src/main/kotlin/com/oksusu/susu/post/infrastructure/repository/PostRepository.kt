package com.oksusu.susu.post.infrastructure.repository

import com.oksusu.susu.category.domain.QCategoryAssignment
import com.oksusu.susu.common.consts.DEFAULT_CATEGORY_ID
import com.oksusu.susu.extension.executeSlice
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.model.PostAndVoteOptionModel
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
    fun countAllByIsActiveAndType(isActive: Boolean, type: PostType): Long
}

interface PostCustomRepository {
    @Transactional(readOnly = true)
    fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel>

    @Transactional(readOnly = true)
    fun getAllVotesExceptBlock(
        isMine: Boolean,
        uid: Long,
        categoryId: Long,
        userBlockIds: List<Long>,
        postBlockIds: List<Long>,
        pageable: Pageable,
    ): Slice<Post>

    @Transactional(readOnly = true)
    fun getAllVotesOrderByPopular(
        isMine: Boolean,
        uid: Long,
        categoryId: Long,
        ids: List<Long>,
        userBlockIds: List<Long>,
        postBlockIds: List<Long>,
    ): List<Post>

    @Transactional(readOnly = true)
    fun findByIsActiveAndTypeAndIdInExceptBlock(
        isActive: Boolean,
        type: PostType,
        ids: List<Long>,
        userBlockId: List<Long>,
        postBlockIds: List<Long>,
    ): List<Post>
}

class PostCustomRepositoryImpl : PostCustomRepository, QuerydslRepositorySupport(Post::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qPost = com.oksusu.susu.post.domain.QPost.post
    private val qVoteOption = com.oksusu.susu.post.domain.QVoteOption.voteOption
    private val qCategoryAssignment = QCategoryAssignment.categoryAssignment

    override fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel> {
        return JPAQuery<com.oksusu.susu.post.domain.QPost>(entityManager)
            .select(com.oksusu.susu.post.infrastructure.repository.model.QPostAndVoteOptionModel(qPost, qVoteOption))
            .from(qPost)
            .leftJoin(qVoteOption).on(qPost.id.eq(qVoteOption.postId))
            .where(
                qPost.id.eq(id),
                qPost.isActive.eq(true),
                qPost.type.eq(PostType.VOTE)
            ).fetch()
    }

    override fun getAllVotesExceptBlock(
        isMine: Boolean,
        uid: Long,
        categoryId: Long,
        userBlockIds: List<Long>,
        postBlockIds: List<Long>,
        pageable: Pageable,
    ): Slice<Post> {
        val uidFilter = qPost.uid.eq(uid).takeIf { isMine } ?: qPost.uid.notIn(userBlockIds)
        val categoryFilter = qCategoryAssignment.categoryId.eq(categoryId).takeIf { categoryId != DEFAULT_CATEGORY_ID }
        val postIdFilter = qPost.id.notIn(postBlockIds)

        val query = JPAQuery<com.oksusu.susu.post.domain.QPost>(entityManager)
            .select(qPost)
            .from(qPost)
            .leftJoin(qCategoryAssignment).on(qPost.id.eq(qCategoryAssignment.targetId))
            .where(
                qPost.isActive.eq(true),
                uidFilter,
                categoryFilter,
                postIdFilter
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
        userBlockIds: List<Long>,
        postBlockIds: List<Long>,
    ): List<Post> {
        val uidFilter = qPost.uid.eq(uid).takeIf { isMine } ?: qPost.uid.notIn(userBlockIds)
        val categoryFilter = qCategoryAssignment.categoryId.eq(categoryId).takeIf { categoryId != DEFAULT_CATEGORY_ID }
        val postIdFilter = qPost.id.notIn(postBlockIds).and(qPost.id.`in`(ids))

        return JPAQuery<com.oksusu.susu.post.domain.QPost>(entityManager)
            .select(qPost)
            .from(qPost)
            .leftJoin(qCategoryAssignment).on(qPost.id.eq(qCategoryAssignment.targetId))
            .where(
                qPost.isActive.eq(true),
                uidFilter,
                categoryFilter,
                postIdFilter
            )
            .fetch()
    }

    override fun findByIsActiveAndTypeAndIdInExceptBlock(
        isActive: Boolean,
        type: PostType,
        ids: List<Long>,
        userBlockId: List<Long>,
        postBlockIds: List<Long>,
    ): List<Post> {
        val uidFilter = qPost.uid.notIn(userBlockId)
        val postIdFilter = qPost.id.`in`(ids).and(qPost.id.notIn(postBlockIds))

        return JPAQuery<com.oksusu.susu.post.domain.QPost>(entityManager)
            .select(qPost)
            .from(qPost)
            .leftJoin(qCategoryAssignment).on(qPost.id.eq(qCategoryAssignment.targetId))
            .where(
                qPost.isActive.eq(isActive),
                qPost.type.eq(type),
                uidFilter,
                postIdFilter
            )
            .fetch()
    }
}

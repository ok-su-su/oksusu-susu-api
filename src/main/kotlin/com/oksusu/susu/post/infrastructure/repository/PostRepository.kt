package com.oksusu.susu.post.infrastructure.repository

import com.oksusu.susu.extension.executeSlice
import com.oksusu.susu.extension.isEquals
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.QPost
import com.oksusu.susu.post.domain.QPostCategory
import com.oksusu.susu.post.domain.QVoteOption
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.model.GetAllVoteSpec
import com.oksusu.susu.post.infrastructure.repository.model.PostAndVoteOptionModel
import com.oksusu.susu.post.infrastructure.repository.model.QPostAndVoteOptionModel
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PostRepository : JpaRepository<Post, Long>, PostCustomRepository {
    @Transactional(readOnly = true)
    fun findByIdAndIsActiveAndType(id: Long, isActive: Boolean, type: PostType): Post?

    @Transactional(readOnly = true)
    fun countAllByIsActiveAndType(isActive: Boolean, type: PostType): Long
}

interface PostCustomRepository {
    @Transactional(readOnly = true)
    fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel>

    @Transactional(readOnly = true)
    fun getAllVotesExceptBlock(spec: GetAllVoteSpec): Slice<Post>

    @Transactional(readOnly = true)
    fun getAllVotesOrderByPopular(
        spec: GetAllVoteSpec,
        ids: List<Long>,
    ): List<Post>

    @Transactional(readOnly = true)
    fun findByIsActiveAndTypeAndIdInExceptBlock(
        isActive: Boolean,
        type: PostType,
        ids: List<Long>,
        userBlockIds: List<Long>,
        postBlockIds: List<Long>,
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
    private val qPostCategory = QPostCategory.postCategory

    override fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel> {
        return JPAQuery<QPost>(entityManager)
            .select(QPostAndVoteOptionModel(qPost, qVoteOption))
            .from(qPost)
            .leftJoin(qVoteOption).on(qPost.id.eq(qVoteOption.postId))
            .where(
                qPost.id.eq(id),
                qPost.isActive.eq(true),
                qPost.type.eq(PostType.VOTE)
            ).fetch()
    }

    override fun getAllVotesExceptBlock(spec: GetAllVoteSpec): Slice<Post> {
        val uidFilter = spec.searchSpec.mine?.let { qPost.uid.eq(spec.uid) } ?: qPost.uid.notIn(spec.userBlockIds)
        val categoryFilter = qPostCategory.id.isEquals(spec.searchSpec.categoryId)
        val contentFilter = spec.searchSpec.content?.let { qPost.content.contains(it) }
        val postIdFilter = qPost.id.notIn(spec.postBlockIds)

        val query = JPAQuery<QPost>(entityManager)
            .select(qPost)
            .from(qPost)
            .join(qPostCategory).on(qPost.postCategoryId.eq(qPostCategory.id))
            .where(
                qPost.isActive.eq(true),
                uidFilter,
                categoryFilter,
                postIdFilter,
                contentFilter
            ).orderBy(
                qPost.createdAt.desc()
            )

        return querydsl.executeSlice(query, spec.pageable)
    }

    override fun getAllVotesOrderByPopular(
        spec: GetAllVoteSpec,
        ids: List<Long>,
    ): List<Post> {
        val postIdFilter = qPost.id.notIn(spec.postBlockIds).and(qPost.id.`in`(ids))
//        val uidFilter = searchSpec.mine?.let { qPost.uid.eq(uid) }
//        val categoryFilter = qCategoryAssignment.categoryId.isEquals(searchSpec.categoryId)
//        val contentFilter = searchSpec.content?.let { qPost.content.contains(it) }
        val uidFilter = qPost.uid.notIn(spec.userBlockIds)

        return JPAQuery<QPost>(entityManager)
            .select(qPost)
            .from(qPost)
            .join(qPostCategory).on(qPost.postCategoryId.eq(qPostCategory.id))
            .where(
                qPost.isActive.eq(true),
                uidFilter,
//                categoryFilter,
                postIdFilter
//                contentFilter
            )
            .fetch()
    }

    override fun findByIsActiveAndTypeAndIdInExceptBlock(
        isActive: Boolean,
        type: PostType,
        ids: List<Long>,
        userBlockIds: List<Long>,
        postBlockIds: List<Long>,
    ): List<Post> {
        val uidFilter = qPost.uid.notIn(userBlockIds)
        val postIdFilter = qPost.id.`in`(ids).and(qPost.id.notIn(postBlockIds))

        return JPAQuery<QPost>(entityManager)
            .select(qPost)
            .from(qPost)
            .join(qPostCategory).on(qPost.postCategoryId.eq(qPostCategory.id))
            .where(
                qPost.isActive.eq(isActive),
                qPost.type.eq(type),
                uidFilter,
                postIdFilter
            )
            .fetch()
    }
}

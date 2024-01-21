package com.oksusu.susu.post.infrastructure.repository

import com.oksusu.susu.count.domain.QCount
import com.oksusu.susu.extension.executeSlice
import com.oksusu.susu.extension.isEquals
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.QPost
import com.oksusu.susu.post.domain.QPostCategory
import com.oksusu.susu.post.domain.QVoteOption
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.model.GetAllVoteSpec
import com.oksusu.susu.post.infrastructure.repository.model.PostAndCountModel
import com.oksusu.susu.post.infrastructure.repository.model.PostAndVoteOptionModel
import com.oksusu.susu.post.infrastructure.repository.model.QPostAndCountModel
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
}

interface PostCustomRepository {
    @Transactional(readOnly = true)
    fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel>

    @Transactional(readOnly = true)
    fun getAllVotesExceptBlock(spec: GetAllVoteSpec): Slice<Post>

    @Transactional(readOnly = true)
    fun getPopularVotesExceptBlock(
        spec: GetAllVoteSpec,
    ): Slice<PostAndCountModel>
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
    private val qCount = QCount.count1

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
        val uidFilter = spec.searchSpec.mine?.let {
            if (it) {
                qPost.uid.eq(spec.uid)
            } else {
                qPost.uid.ne(spec.uid)
            }
        } ?: qPost.uid.notIn(spec.userBlockIds)
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

    override fun getPopularVotesExceptBlock(
        spec: GetAllVoteSpec,
    ): Slice<PostAndCountModel> {
        val uidFilter = spec.searchSpec.mine?.let {
            if (it) {
                qPost.uid.eq(spec.uid)
            } else {
                qPost.uid.ne(spec.uid)
            }
        } ?: qPost.uid.notIn(spec.userBlockIds)
        val categoryFilter = qPostCategory.id.isEquals(spec.searchSpec.categoryId)
        val contentFilter = spec.searchSpec.content?.let { qPost.content.contains(it) }
        val postIdFilter = qPost.id.notIn(spec.postBlockIds)

        val query = JPAQuery<QPost>(entityManager)
            .select(
                QPostAndCountModel(
                    qPost,
                    qCount
                )
            )
            .from(qPost)
            .join(qPostCategory).on(qPost.postCategoryId.eq(qPostCategory.id))
            .join(qCount).on(qPost.id.eq(qCount.targetId))
            .where(
                qPost.type.eq(PostType.VOTE),
                qPost.isActive.eq(true),
                uidFilter,
                categoryFilter,
                postIdFilter,
                contentFilter
            )
            .orderBy(qCount.count.desc())

        return querydsl.executeSlice(query, spec.pageable)
    }
}

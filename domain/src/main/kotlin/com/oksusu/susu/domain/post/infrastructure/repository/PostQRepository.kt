package com.oksusu.susu.domain.post.infrastructure.repository

import com.oksusu.susu.domain.common.extension.executeSlice
import com.oksusu.susu.domain.common.extension.isContains
import com.oksusu.susu.domain.common.extension.isEquals
import com.oksusu.susu.domain.common.extension.isNotIn
import com.oksusu.susu.domain.count.domain.QCount
import com.oksusu.susu.domain.count.domain.vo.CountTargetType
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.QPost
import com.oksusu.susu.domain.post.domain.QVoteOption
import com.oksusu.susu.domain.post.domain.vo.PostType
import com.oksusu.susu.domain.post.infrastructure.repository.model.*
import com.oksusu.susu.domain.user.domain.QUser
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
interface PostQRepository {
    fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel>

    fun getVoteAndCountExceptBlock(spec: GetVoteSpec): Slice<PostAndVoteCountModel>

    fun getPostAndCreator(id: Long, type: PostType): PostAndUserModel?

    fun updateIsActiveById(ids: List<Long>): Long
}

class PostQRepositoryImpl : PostQRepository, QuerydslRepositorySupport(Post::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qPost = QPost.post
    private val qVoteOption = QVoteOption.voteOption
    private val qCount = QCount.count1
    private val qUser = QUser.user

    override fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel> {
        return JPAQuery<QPost>(entityManager)
            .select(QPostAndVoteOptionModel(qPost, qVoteOption))
            .from(qPost)
            .join(qVoteOption).on(qPost.id.eq(qVoteOption.postId))
            .where(
                qPost.id.eq(id),
                qPost.isActive.eq(true),
                qPost.type.eq(PostType.VOTE)
            ).fetch()
    }

    override fun getVoteAndCountExceptBlock(spec: GetVoteSpec): Slice<PostAndVoteCountModel> {
        val uidFilter = spec.searchSpec.mine?.takeIf { mine -> mine }?.let {
            qPost.uid.isEquals(spec.uid)
        } ?: qPost.uid.isNotIn(spec.userBlockIds)
        val boardFilter = qPost.boardId.isEquals(spec.searchSpec.boardId)
        val contentFilter = qPost.content.isContains(spec.searchSpec.content)
        val postIdFilter = qPost.id.isNotIn(spec.postBlockIds)

        val orders = when (spec.searchSpec.sortType) {
            VoteSortType.POPULAR -> arrayOf(qCount.count.desc())
            VoteSortType.LATEST -> emptyArray()
        }

        val query = JPAQuery<QPost>(entityManager)
            .select(
                QPostAndVoteCountModel(
                    qPost,
                    qCount.count
                )
            )
            .from(qPost)
            .join(qCount).on(qCount.targetType.eq(CountTargetType.POST).and(qPost.id.eq(qCount.targetId)))
            .where(
                qPost.type.eq(PostType.VOTE),
                qPost.isActive.eq(true),
                uidFilter,
                boardFilter,
                postIdFilter,
                contentFilter
            ).orderBy(*orders)

        return querydsl.executeSlice(query, spec.pageable)
    }

    override fun getPostAndCreator(id: Long, type: PostType): PostAndUserModel? {
        return JPAQuery<QPost>(entityManager)
            .select(QPostAndUserModel(qPost, qUser))
            .from(qPost)
            .join(qUser).on(qPost.uid.eq(qUser.id))
            .where(
                qPost.id.eq(id),
                qPost.isActive.eq(true),
                qPost.type.eq(type)
            ).fetchFirst()
    }

    override fun updateIsActiveById(ids: List<Long>): Long {
        return JPAQueryFactory(entityManager)
            .update(qPost)
            .where(qPost.id.`in`(ids))
            .set(qPost.isActive, false)
            .execute()
    }
}

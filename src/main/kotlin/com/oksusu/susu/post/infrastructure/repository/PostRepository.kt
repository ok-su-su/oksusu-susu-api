package com.oksusu.susu.post.infrastructure.repository

import com.oksusu.susu.count.domain.QCount
import com.oksusu.susu.count.domain.vo.CountTargetType
import com.oksusu.susu.extension.executeSlice
import com.oksusu.susu.extension.isContains
import com.oksusu.susu.extension.isEquals
import com.oksusu.susu.extension.isNotIn
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.QPost
import com.oksusu.susu.post.domain.QVoteOption
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.model.GetAllVoteSpec
import com.oksusu.susu.post.infrastructure.repository.model.PostAndCountModel
import com.oksusu.susu.post.infrastructure.repository.model.PostAndVoteOptionModel
import com.oksusu.susu.post.infrastructure.repository.model.QPostAndCountModel
import com.oksusu.susu.post.infrastructure.repository.model.QPostAndVoteOptionModel
import com.oksusu.susu.post.model.vo.VoteSortType
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
    fun getAllVotesExceptBlock(spec: GetAllVoteSpec): Slice<PostAndCountModel>
}

class PostCustomRepositoryImpl : PostCustomRepository, QuerydslRepositorySupport(Post::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qPost = QPost.post
    private val qVoteOption = QVoteOption.voteOption
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

    override fun getAllVotesExceptBlock(spec: GetAllVoteSpec): Slice<PostAndCountModel> {
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
                QPostAndCountModel(
                    qPost,
                    qCount
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
}

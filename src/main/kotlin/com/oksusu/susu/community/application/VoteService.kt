package com.oksusu.susu.community.application

import com.oksusu.susu.common.dto.SusuSliceRequest
import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.domain.vo.CommunityType
import com.oksusu.susu.community.infrastructure.repository.CommunityRepository
import com.oksusu.susu.community.infrastructure.repository.model.CommunityAndVoteOptionModel
import com.oksusu.susu.community.model.vo.VoteSortType
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NoAuthorityException
import com.oksusu.susu.exception.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import kotlin.math.E

@Service
class VoteService(
    private val communityService: CommunityService,
    private val communityRepository: CommunityRepository,
) {
    suspend fun getAllVotes(
        isMine: Boolean,
        uid: Long,
        category: CommunityCategory,
        pageable: Pageable,
    ): Slice<Community> {
        return withContext(Dispatchers.IO) {
            communityRepository.getAllVotes(isMine, uid, category, pageable)
        }
    }

    suspend fun getVote(id: Long): Community {
        return communityService.findByIdAndIsActiveAndTypeOrThrow(id, true, CommunityType.VOTE)
    }

    suspend fun getVoteAndOptions(id: Long): List<CommunityAndVoteOptionModel> {
        return withContext(Dispatchers.IO) {
            communityRepository.getVoteAndOptions(id)
        }.takeUnless { it.isNullOrEmpty() } ?: throw NotFoundException(ErrorCode.NOT_FOUND_VOTE_ERROR)
    }

    suspend fun softDeleteVote(uid: Long, id: Long): Community {
        val vote = getVote(id)

        if (vote.uid != uid) {
            throw NoAuthorityException(ErrorCode.NO_AUTHORITY_ERROR)
        }

        vote.apply { isActive = false }

        return vote
    }

    suspend fun getAllVotesByIdIn(communityIds: List<Long>): List<Community> {
        return communityService.findByIsActiveAndTypeAndIdIn(true, CommunityType.VOTE, communityIds)
    }

    suspend fun getAllVotesOrderByPopular(
        isMine: Boolean,
        uid: Long,
        category: CommunityCategory,
        ids: List<Long>,
        pageable: Pageable,
    ): Slice<Community> {
        val customPageRequest = PageRequest.of(0, pageable.pageSize)
        val (votes, totalCount) = withContext(Dispatchers.IO) {
            val voteDeferred =
                async { communityRepository.getAllVotesOrderByPopular(isMine, uid, category, ids, customPageRequest) }
            val countDeferred = async { getActiveVoteCount() }

            voteDeferred.await() to countDeferred.await()
        }

        val sortedContent = ids.flatMap { id -> votes.filter { it.id == id } }
        val hasNext = (pageable.pageSize * pageable.pageNumber < totalCount
                && totalCount <= (pageable.pageSize + 1) * pageable.pageNumber)
        return SliceImpl(sortedContent, pageable, hasNext)
    }

    suspend fun getActiveVoteCount(): Long {
        return communityService.countAllByIsActiveAndType(true, CommunityType.VOTE)
    }
}

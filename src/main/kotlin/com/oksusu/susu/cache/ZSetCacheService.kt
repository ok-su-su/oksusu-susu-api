package com.oksusu.susu.cache

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.ZSetOperations

interface ZSetCacheService {
    suspend fun <T> zSetSaveAll(key: String, tuples: Map<T, Long>)
    suspend fun <T> zSetSave(key: String, tuple: Map<T, Long>)
    suspend fun zSetFindByMember(key: String, member: String): Double
    suspend fun zSetFindByMemberIn(key: String, members: List<String>): List<Double>
    suspend fun zSetFindRangeWithScores(key: String, range: Range<Long>): Flow<ZSetOperations.TypedTuple<String>>
    suspend fun zSetDeleteByMemberIn(key: String, members: List<String>)
}

package com.oksusu.susu.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Service

@Service
class CacheServiceImpl(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
): CacheService {
    val zSetOps = reactiveRedisTemplate.opsForZSet()

    override suspend fun <T> zSetSaveAll(key: String, tuples: Map<T, Long>) {
        val typedTuples = arrayListOf<ZSetOperations.TypedTuple<String>>()
        tuples.forEach { tuple ->
            typedTuples.add(
                DefaultTypedTuple(
                    tuple.key.toString(),
                    tuple.value.toDouble()
                )
            )
        }

        zSetOps.addAll(key, typedTuples).awaitSingle()
    }

    override suspend fun <T> zSetSave(key: String, tuple: Map<T, Long>) {
        val member = tuple.firstNotNullOf { it.key.toString() }
        val score = tuple.firstNotNullOf { it.value.toDouble() }

        zSetOps.add(key, member, score).awaitSingle()
    }

    override suspend fun zSetFindByMember(key: String, member: String): Double {
        return zSetOps.score(key, member).awaitSingle()
    }

    override suspend fun zSetFindByMemberIn(key: String, members: List<String>): List<Double> {
        return zSetOps.score(key, *members.toTypedArray()).awaitSingle()
    }

    override suspend fun zSetFindRangeWithScores(key: String, range: Range<Long>): Flow<ZSetOperations.TypedTuple<String>> {
        return zSetOps.rangeWithScores(key, range).asFlow()
    }

    override suspend fun zSetDeleteByMemberIn(key: String, members: List<String>) {
        zSetOps.remove(key, *members.toTypedArray()).awaitSingle()
    }
}

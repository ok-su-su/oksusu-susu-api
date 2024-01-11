package com.oksusu.susu.cache

import com.oksusu.susu.cache.model.ZSetModel
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToExecuteException
import com.oksusu.susu.extension.mapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.zip

@Service
class SuspendableCacheService(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
) : CacheService {
    private val logger = mu.KotlinLogging.logger { }
    private val zSetOps = reactiveRedisTemplate.opsForZSet()
    private val keyValueOps = reactiveRedisTemplate.opsForValue()

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

    override suspend fun zSetFindRangeWithScores(
        key: String,
        range: Range<Long>,
    ): Flow<ZSetOperations.TypedTuple<String>> {
        return zSetOps.rangeWithScores(key, range).asFlow()
    }

    override suspend fun zSetDeleteByMemberIn(key: String, members: List<String>) {
        zSetOps.remove(key, *members.toTypedArray())
            .awaitSingle()
    }

    override suspend fun <VALUE_TYPE : Any> set(cache: Cache<VALUE_TYPE>, value: VALUE_TYPE) {
        coroutineScope {
            launch(Dispatchers.IO + Job()) {
                runCatching {
                    reactiveStringRedisTemplate.opsForValue().set(
                        cache.key,
                        mapper.writeValueAsString(value),
                        cache.duration
                    ).cache().awaitSingleOrNull()
                }.onFailure { e ->
                    when (e) {
                        is CancellationException -> logger.debug { "Redis Set job cancelled." }
                        else -> logger.error(e) { "fail to set data from redis. key : ${cache.key}" }
                    }
                }.getOrNull()
            }
        }
    }

    override suspend fun <VALUE_TYPE : Any> getOrNull(cache: Cache<VALUE_TYPE>): VALUE_TYPE? {
        return runCatching {
            val jsonValue = reactiveStringRedisTemplate.opsForValue()
                .get(cache.key)
                .cache()
                .awaitSingleOrNull()

            when (jsonValue.isNullOrBlank()) {
                true -> null
                false -> mapper.readValue(jsonValue, cache.type)
            }
        }.onFailure { e ->
            when (e) {
                is CancellationException -> logger.debug { "Redis Read job cancelled." }
                else -> logger.error(e) { "fail to read data from redis. key : ${cache.key}" }
            }
        }.getOrNull()
    }

    override suspend fun <VALUE_TYPE : Any> zSetAll(cache: ZSetCache<VALUE_TYPE>, tuples: Map<VALUE_TYPE, Double>) {
        coroutineScope {
            launch(Dispatchers.IO + Job()) {
                runCatching {
                    val typedTuples = arrayListOf<ZSetOperations.TypedTuple<String>>()
                    tuples.forEach { tuple ->
                        typedTuples.add(
                            DefaultTypedTuple(
                                tuple.key.toString(),
                                tuple.value
                            )
                        )
                    }

                    reactiveStringRedisTemplate.opsForZSet().addAll(
                        cache.key,
                        typedTuples
                    ).cache().awaitSingleOrNull()
                }.onFailure { e ->
                    when (e) {
                        is CancellationException -> logger.debug { "Redis Set job cancelled." }
                        else -> logger.error(e) { "fail to set data from redis. key : ${cache.key}" }
                    }
                }.getOrNull()
            }
        }
    }

    override suspend fun <VALUE_TYPE : Any> zGetByMembers(
        cache: ZSetCache<VALUE_TYPE>,
        members: List<VALUE_TYPE>,
    ): List<Double> {
        return runCatching {
            val jsonMembers = members.map { member -> mapper.writeValueAsString(member) }.toTypedArray()

            reactiveStringRedisTemplate.opsForZSet()
                .score(cache.key, *jsonMembers)
                .cache()
                .awaitSingleOrNull()
        }.onFailure { e ->
            when (e) {
                is CancellationException -> logger.debug { "Redis Read job cancelled." }
                else -> logger.error(e) { "fail to read data from redis. key : ${cache.key}" }
            }
        }.getOrNull() ?: throw FailToExecuteException(ErrorCode.FAIL_TO_REDIS_EXECUTE_ERROR)
    }

    override suspend fun <VALUE_TYPE : Any> zGetByRange(
        cache: ZSetCache<VALUE_TYPE>,
        range: Range<Long>,
    ): List<ZSetModel<String>> {
        return runCatching {
            reactiveStringRedisTemplate.opsForZSet()
                .rangeWithScores(cache.key, range)
                .cache()
                .asFlow().map { zSet ->
                    ZSetModel(
                        key = zSet.value,
                        value = zSet.value,
                        score = zSet.score
                    )
                }.toList()

        }.onFailure { e ->
            when (e) {
                is CancellationException -> logger.debug { "Redis Read job cancelled." }
                else -> logger.error(e) { "fail to read data from redis. key : ${cache.key}" }
            }
        }.getOrNull() ?: throw FailToExecuteException(ErrorCode.FAIL_TO_REDIS_EXECUTE_ERROR)
    }

    override suspend fun <VALUE_TYPE : Any> zDeleteByMembers(cache: ZSetCache<VALUE_TYPE>, members: List<VALUE_TYPE>) {
        runCatching {
            val jsonValues = members.map { member -> mapper.writeValueAsString(member) }.toTypedArray()

            reactiveStringRedisTemplate.opsForZSet()
                .remove(cache.key, *jsonValues)
                .cache()
                .awaitSingleOrNull()
        }.onFailure { e ->
            when (e) {
                is CancellationException -> logger.debug { "Redis Delete job cancelled." }
                else -> logger.error(e) { "fail to delete data from redis. key : ${cache.key}" }
            }
        }.getOrNull()
    }
}

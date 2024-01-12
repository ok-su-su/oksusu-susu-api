package com.oksusu.susu.cache

import com.oksusu.susu.cache.model.ZSetModel
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToExecuteException
import com.oksusu.susu.extension.mapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service

@Service
class SuspendableCacheService(
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
) : CacheService {
    private val logger = mu.KotlinLogging.logger { }
    private val zSetOps = reactiveStringRedisTemplate.opsForZSet()
    private val keyValueOps = reactiveStringRedisTemplate.opsForValue()

    override suspend fun <VALUE_TYPE : Any> set(cache: Cache<VALUE_TYPE>, value: VALUE_TYPE) {
        coroutineScope {
            launch(Dispatchers.IO + Job()) {
                runCatching {
                    keyValueOps.set(
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
            val jsonValue = keyValueOps
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
                    val typedTuples = tuples.map { tuple ->
                        DefaultTypedTuple(
                            mapper.writeValueAsString(tuple.key),
                            tuple.value
                        )
                    }

                    zSetOps.addAll(
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

            zSetOps.score(cache.key, *jsonMembers)
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
    ): List<ZSetModel<VALUE_TYPE>> {
        return runCatching {
            zSetOps.rangeWithScores(cache.key, range)
                .cache()
                .asFlow().map { zSet ->
                    ZSetModel(
                        key = cache.key,
                        value = mapper.readValue(zSet.value, cache.type),
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

            zSetOps.remove(cache.key, *jsonValues)
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

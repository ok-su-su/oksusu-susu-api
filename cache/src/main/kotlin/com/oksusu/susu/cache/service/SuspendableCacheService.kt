package com.oksusu.susu.cache.service

import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.common.extension.mapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service

@Service
class SuspendableCacheService(
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
) : CacheService {
    private val logger = KotlinLogging.logger { }
    private val keyValueOps = reactiveStringRedisTemplate.opsForValue()

    override suspend fun <VALUE_TYPE : Any> set(cache: Cache<VALUE_TYPE>, value: VALUE_TYPE) {
        coroutineScope {
            launch(Dispatchers.IO + Job() + MDCContext()) {
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

    override suspend fun <VALUE_TYPE : Any> delete(cache: Cache<VALUE_TYPE>) {
        runCatching {
            keyValueOps.delete(cache.key)
                .awaitSingle()
        }.onFailure { e ->
            when (e) {
                is CancellationException -> logger.debug { "Redis Delete job cancelled." }
                else -> logger.error(e) { "fail to delete data from redis. key : ${cache.key}" }
            }
        }.getOrNull()
    }
}

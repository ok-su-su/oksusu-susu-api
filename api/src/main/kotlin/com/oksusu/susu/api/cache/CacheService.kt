package com.oksusu.susu.api.cache

import com.oksusu.susu.api.cache.model.ZSetModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.data.domain.Range
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface CacheService {
    /** key-value */
    suspend fun <VALUE_TYPE : Any> set(cache: Cache<VALUE_TYPE>, value: VALUE_TYPE)
    suspend fun <VALUE_TYPE : Any> getOrNull(cache: Cache<VALUE_TYPE>): VALUE_TYPE?
    suspend fun <VALUE_TYPE : Any> delete(cache: Cache<VALUE_TYPE>)

    /** sorted set */
    suspend fun <VALUE_TYPE : Any> zGetByMembers(
        cache: ZSetCache<VALUE_TYPE>,
        members: List<VALUE_TYPE>,
    ): List<Double>

    suspend fun <VALUE_TYPE : Any> zGetByRange(
        cache: ZSetCache<VALUE_TYPE>,
        range: Range<Long>,
    ): List<ZSetModel<VALUE_TYPE>>

    suspend fun <VALUE_TYPE : Any> zSetAll(cache: ZSetCache<VALUE_TYPE>, tuples: Map<VALUE_TYPE, Double>)
    suspend fun <VALUE_TYPE : Any> zDeleteByMembers(cache: ZSetCache<VALUE_TYPE>, members: List<VALUE_TYPE>)

    companion object {
        /** key-value */
        suspend fun <VALUE_TYPE : Any> CacheService.set(
            cache: Cache.Factory.() -> Cache<VALUE_TYPE>,
            value: VALUE_TYPE,
        ) = set(cache(Cache), value)

        suspend fun <VALUE_TYPE : Any> CacheService.getOrNull(
            cache: Cache.Factory.() -> Cache<VALUE_TYPE>,
        ) = getOrNull(cache(Cache))

        suspend fun <VALUE_TYPE : Any> CacheService.get(
            cache: Cache.Factory.() -> Cache<VALUE_TYPE>,
            onCacheMissResume: suspend () -> VALUE_TYPE,
        ): VALUE_TYPE = cache(Cache).let { cacheInfo ->
            getOrNull(cacheInfo) ?: onCacheMissResume().also { value -> launchSet(coroutineContext, cacheInfo, value) }
        }

        suspend fun <VALUE_TYPE : Any> CacheService.launchSet(
            context: CoroutineContext = EmptyCoroutineContext,
            cache: Cache<VALUE_TYPE>,
            value: VALUE_TYPE,
        ) = CoroutineScope(context + Job()).launch { set(cache, value) }

        /** sorted set */
        suspend fun <VALUE_TYPE : Any> CacheService.zSet(
            cache: ZSetCache.Factory.() -> ZSetCache<VALUE_TYPE>,
            member: VALUE_TYPE,
            score: Double,
        ) = zSetAll(cache(ZSetCache), mapOf(member to score))

        suspend fun <VALUE_TYPE : Any> CacheService.zSetAll(
            cache: ZSetCache.Factory.() -> ZSetCache<VALUE_TYPE>,
            tuples: Map<VALUE_TYPE, Double>,
        ) = zSetAll(cache(ZSetCache), tuples)

        suspend fun <VALUE_TYPE : Any> CacheService.zGetByMemberOrNull(
            cache: ZSetCache.Factory.() -> ZSetCache<VALUE_TYPE>,
            member: VALUE_TYPE,
        ) = zGetByMembers(cache(ZSetCache), listOf(member)).firstOrNull()

        suspend fun <VALUE_TYPE : Any> CacheService.zGetByMembers(
            cache: ZSetCache.Factory.() -> ZSetCache<VALUE_TYPE>,
            members: List<VALUE_TYPE>,
        ) = zGetByMembers(cache(ZSetCache), members)

        suspend fun <VALUE_TYPE : Any> CacheService.zGetByRange(
            cache: ZSetCache.Factory.() -> ZSetCache<VALUE_TYPE>,
            range: Range<Long>,
        ) = zGetByRange(cache(ZSetCache), range)

        suspend fun <VALUE_TYPE : Any> CacheService.zDeleteByMember(
            cache: ZSetCache.Factory.() -> ZSetCache<VALUE_TYPE>,
            member: VALUE_TYPE,
        ) = zDeleteByMembers(cache(ZSetCache), listOf(member))

        suspend fun <VALUE_TYPE : Any> CacheService.zDeleteByMembers(
            cache: ZSetCache.Factory.() -> ZSetCache<VALUE_TYPE>,
            members: List<VALUE_TYPE>,
        ) = zDeleteByMembers(cache(ZSetCache), members)
    }
}

package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.event.model.CacheAppleOidcPublicKeysEvent
import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.common.extension.withMDCContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@SusuEventListener
class AuthEventListener(
    private val cacheService: CacheService,
) {
    val logger = KotlinLogging.logger { }

    @TransactionalEventListener
    fun cacheAppleOidcPublicKeysService(event: CacheAppleOidcPublicKeysEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
            logger.info { "[${event.publishAt}] apple oidc pub key 캐싱 시작" }

            withMDCContext(Dispatchers.IO) {
                cacheService.set(Cache.getAppleOidcPublicKeyCache(), event.keys)
            }

            logger.info { "[${event.publishAt}] apple oidc pub key 캐싱 끝" }
        }
    }
}

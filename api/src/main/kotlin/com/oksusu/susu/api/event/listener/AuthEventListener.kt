package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.event.model.CacheAppleOidcPublicKeysEvent
import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.common.extension.withMDCContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class AuthEventListener(
    private val cacheService: CacheService,
) {
    val logger = KotlinLogging.logger { }

    @TransactionalEventListener
    fun cacheAppleOidcPublicKeysService(event: CacheAppleOidcPublicKeysEvent) {
        CoroutineScope(Dispatchers.IO + Job()).launch {
            logger.info { "[${event.publishAt}] apple oidc pub key 캐싱 시작" }

            withMDCContext(Dispatchers.IO) {
                cacheService.set(Cache.getAppleOidcPublicKeyCache(), event.keys)
            }

            logger.info { "[${event.publishAt}] apple oidc pub key 캐싱 끝" }
        }
    }
}

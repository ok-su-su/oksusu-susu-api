package com.oksusu.susu.config.async

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@Configuration
class AsyncConfig(
    private val asyncExceptionHandler: AsyncExceptionHandler,
) : AsyncConfigurer {
    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler? {
        return asyncExceptionHandler
    }
}
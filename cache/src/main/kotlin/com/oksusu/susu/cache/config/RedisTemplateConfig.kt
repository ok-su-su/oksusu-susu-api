package com.oksusu.susu.cache.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisTemplateConfig(
    private val redisConnectionFactory: ReactiveRedisConnectionFactory,
) {
    @Bean
    fun reactiveRedisTemplate(): ReactiveRedisTemplate<String, String> {
        val serializer = RedisSerializationContext
            .newSerializationContext<String, String>(JdkSerializationRedisSerializer())
            .key(StringRedisSerializer.UTF_8)
            .value(StringRedisSerializer.UTF_8)
            .build()

        return ReactiveRedisTemplate(redisConnectionFactory, serializer)
    }
}

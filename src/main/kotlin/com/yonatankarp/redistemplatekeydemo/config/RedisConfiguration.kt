package com.yonatankarp.redistemplatekeydemo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate


@Configuration
class RedisConfiguration {
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Int> =
        RedisTemplate<String, Int>()
            .apply {
                this.connectionFactory = connectionFactory
            }
}

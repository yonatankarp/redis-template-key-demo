package com.yonatankarp.redistemplatekeydemo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericToStringSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfiguration {
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Int> =
        RedisTemplate<String, Int>()
            .apply {
                this.connectionFactory = connectionFactory
                this.keySerializer = StringRedisSerializer()
                this.valueSerializer = GenericToStringSerializer(Int::class.java)
            }
}

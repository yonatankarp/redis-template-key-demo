package com.yonatankarp.redistemplatekeydemo

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.testcontainers.containers.GenericContainer

@SpringBootTest
@TestConstructor(autowireMode = ALL)
class RedisContainerTest(private val redisTemplate: RedisTemplate<String, Int>) {

    companion object {
        private const val KEY = "key"
        private const val VALUE = 1234
        private const val REDIS_PORT = 6379

        private val redisContainer = GenericContainer<Nothing>("redis:latest")
            .apply {
                withExposedPorts(REDIS_PORT)
                start()
            }

        @DynamicPropertySource
        @JvmStatic
        @Suppress("unused")
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(REDIS_PORT) }
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            redisContainer.stop()
        }
    }

    @BeforeEach
    fun setup() {
        redisTemplate.delete(redisTemplate.keys("*"))
        redisContainer.execInContainer(
            "redis-cli",
            "SET",
            KEY,
            VALUE.toString()
        )
    }

    @Test
    fun `should fail to fetch value`() {
        // Given predefined int key in Redis

        // When we fetch the value
        val value = redisTemplate.opsForValue().get(KEY)

        // Then the value is null
        assertNull(value)
    }

    @Test
    fun `should successfully fetch the value`() {
        // Given a value stored via RedisTemplate
        redisTemplate.opsForValue().set(KEY, VALUE + 1)

        // When we fetch the value
        val value = redisTemplate.opsForValue().get(KEY)

        // Then the value is null
        assertEquals(VALUE + 1, value)
    }
}

# `RedisTemplate` Value Fetching Bug Demo

### TL;DR

> This repository shows an example in which using the `RedisTemplate` of Spring.
>
> This repository shows how by wrongly configuring your code, you can create a
> test that will pass, while actually failing to fetch the correct information
> from Redis.

### Introduction

To configure our application to connect to Redis, we will add the following
configurations:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

We will now create our `RedisTemplate` bean will be used as a Redis client in
our codebase.

```kotlin
@Configuration
class RedisConfiguration {
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Int> =
        RedisTemplate<String, Int>()
            .apply {
                this.connectionFactory = connectionFactory
            }
}
```

In the next step, we will create a test that uses Redis test container:

```kotlin
@SpringBootTest
@TestConstructor(autowireMode = ALL)
class RedisContainerTest(private val redisTemplate: RedisTemplate<String, Int>) {
    companion object {
        private const val REDIS_PORT = 6379

        private val redisContainer = GenericContainer<Nothing>("redis:latest")
            .apply {
                withExposedPorts(REDIS_PORT)
                start()
            }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            redisContainer.stop()
        }
    }
}
```

We will set our Redis host & port to the values of our test container next:


```kotlin
        @DynamicPropertySource
        @JvmStatic
        @Suppress("unused")
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(REDIS_PORT) }
        }
```

and now, we will ensure to truncate our Redis cache between tests, and insert a
predefined key & value to redis using the `redis-cli` tool.

```kotlin
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
```

By running the following test with a breakpoint, and checking the container
running the command, we can see that the key is indeed stored in our container.


```shell
$ docker exec -it <CONTAINER_NAME> redis-cli
127.0.0.1:6379> keys *
1) "key"
```

Now, we will write two tests, the first will try to fetch the value from the
container directly using our `RedisTemplate` bean.

```kotlin
@Test
fun `should fail to fetch value`() {
    // Given predefined int key in Redis

    // When we fetch the value
    val value = redisTemplate.opsForValue().get(KEY)

    // Then the value is null
    assertNull(value)
}
```

The second will update the value of the key using the `RedisTemplate` and fetch
the key again using the template

```kotlin
@Test
fun `should successfully fetch the value`() {
    // Given a value stored via RedisTemplate
    redisTemplate.opsForValue().set(KEY, VALUE + 1)

    // When we fetch the value
    val value = redisTemplate.opsForValue().get(KEY)

    // Then the value is null
    assertEquals(VALUE + 1, value)
}
```

As you can see, the first test fails to find the key in Redis, and hence returns
a `null` value, while the second can find the key after we used our template to
update the value.

You can reproduce this issue by running the codebase in the
[main](https://github.com/yonatankarp/redis-template-key-demo) branch.

To fix the issue, you should simply add to the `RedisTemplate` definition the
key and value serializers as follows:

```kotlin
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
```

You can now see that by running the following test, our code will manage to
fetch the key successfully:

```kotlin
@Test
fun `should fail to fetch value`() {
    // Given predefined int key in Redis

    // When we fetch the value
    val value = redisTemplate.opsForValue().get(KEY)

    // Then the value is correctly fetched
    assertEquals(VALUE, value)
}
```

You can run the code yourself from the branch
[value-fetching-fix](https://github.com/yonatankarp/redis-template-key-demo/tree/value-fetching-fix).

## Pre-requirements

* JVM 21 or newer
* Gradle 8 or newer
* Docker

## How to run locally?

To build this project, you can simply run the following command in your
terminal:

```shell
$ ./gradlew build
```

However, as the project does not actually build any valuable executable, it
would probably make more sense to run the
[RedisContainerTest](src/test/kotlin/com/yonatankarp/redistemplatekeydemo/RedisContainerTest.kt)
instead.

Running the test will launch a new Redis container that will be used for the
different tests.

## Technologies

This repository uses the following technologies:

* Kotlin
* SpringBoot
* TestContainers
* jUnit
* Gradle

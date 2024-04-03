package com.yonatankarp.redistemplatekeydemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedisTemplateKeyDemoApplication

fun main(args: Array<String>) {
    runApplication<RedisTemplateKeyDemoApplication>(*args)
}

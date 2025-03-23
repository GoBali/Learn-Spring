package org.example.learnspring

import OpenAPIConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Import

@SpringBootApplication
@EnableCaching
@Import(OpenAPIConfig::class)
class LearnSpringApplication

fun main(args: Array<String>) {
	runApplication<LearnSpringApplication>(*args)
}

package org.example.learnspring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class LearnSpringApplication

fun main(args: Array<String>) {
	runApplication<LearnSpringApplication>(*args)
}

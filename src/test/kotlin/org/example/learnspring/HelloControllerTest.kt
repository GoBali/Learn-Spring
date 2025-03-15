package org.example.learnspring

import org.junit.jupiter.api.Test
import org.example.learnspring.controller.HelloController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@WebMvcTest(
    controllers = [HelloController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class HelloControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `sayHello returns expected greeting`() {
        mockMvc.get("/api/hello")
            .andExpect {
                status { isOk() }
                content { string("Hello, Kotlin Spring Web Server!") }
            }
    }
}
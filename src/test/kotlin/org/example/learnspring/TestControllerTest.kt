package org.example.learnspring

import org.junit.jupiter.api.Test
import org.example.learnspring.controller.TestController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@WebMvcTest(
    controllers = [TestController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class TestControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `hello returns expected greeting`() {
        mockMvc.get("/api/hello")
            .andExpect {
                status { isOk() }
//                content { string("Hello, Kotlin Spring Web Server!") }
            }
    }
}
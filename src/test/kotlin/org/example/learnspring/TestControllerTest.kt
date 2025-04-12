package org.example.learnspring

import org.junit.jupiter.api.Test
import org.example.learnspring.controller.TestController
import org.example.learnspring.utility.EncryptionUtility
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@WebMvcTest(
    controllers = [TestController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class TestControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var encryptionUtility: EncryptionUtility

    @Test
    fun `hello returns expected greeting`() {
        given(encryptionUtility.encrypt("Hello World!"))
            .willReturn("Encrypted Hello World!")

        mockMvc.get("/api/hello")
            .andExpect {
                status { isOk() }
            }
    }
}
package org.example.learnspring.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.learnspring.dto.CreateUserRequest
import org.example.learnspring.dto.DeleteUserRequest
import org.example.learnspring.dto.UpdateUserRequest
import org.example.learnspring.dto.UserDto
import org.example.learnspring.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.delete

@WebMvcTest(
    controllers = [UserController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class UserControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var userService: UserService

    @Test
    fun `getAllUsers returns list of users`() {
        // Given
        val users = listOf(
            UserDto("User 1", "user1@example.com"),
            UserDto("User 2", "user2@example.com")
        )
        given(userService.getAllUsers()).willReturn(users)

        // When/Then
        mockMvc.get("/api/users")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(objectMapper.writeValueAsString(users)) }
            }
    }

    @Test
    fun `getUserById returns user when found`() {
        // Given
        val userId = 1L
        val user = UserDto("Test User", "test@example.com")
        given(userService.getUserById(userId)).willReturn(user)

        // When/Then
        mockMvc.get("/api/users/$userId")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(objectMapper.writeValueAsString(user)) }
            }
    }

    @Test
    fun `createUser creates and returns new user`() {
        // Given
        val createRequest = CreateUserRequest("New User", "new@example.com", "password123")
        val createdUser = UserDto("New User", "new@example.com")
        given(userService.createUser(createRequest)).willReturn(createdUser)

        // When/Then
        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createRequest)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(objectMapper.writeValueAsString(createdUser)) }
        }

        verify(userService).createUser(createRequest)
    }

    @Test
    fun `updateUser updates and returns user`() {
        // Given
        val email = "existing@example.com"
        val updateRequest = UpdateUserRequest("Updated Name", "password123")
        val updatedUser = UserDto("Updated Name", email)
        given(userService.updateUser(email, updateRequest)).willReturn(updatedUser)

        // When/Then
        mockMvc.put("/api/users/$email") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(objectMapper.writeValueAsString(updatedUser)) }
        }

        verify(userService).updateUser(email, updateRequest)
    }

    @Test
    fun `deleteUser deletes user and returns success message`() {
        // Given
        val email = "delete@example.com"
        val deleteRequest = DeleteUserRequest("password123")
        val response = mapOf("message" to "User deleted successfully", "email" to email)
        given(userService.deleteUser(email, deleteRequest)).willReturn(response)

        // When/Then
        mockMvc.delete("/api/users/$email") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(deleteRequest)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(objectMapper.writeValueAsString(response)) }
        }

        verify(userService).deleteUser(email, deleteRequest)
    }
}

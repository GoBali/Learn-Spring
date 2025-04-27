package org.example.learnspring.service

import io.micrometer.tracing.Span
import io.micrometer.tracing.Tracer
import org.example.learnspring.dto.CreateUserRequest
import org.example.learnspring.dto.DeleteUserRequest
import org.example.learnspring.dto.UpdateUserRequest
import org.example.learnspring.entity.User
import org.example.learnspring.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

class UserServiceTest {

    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var tracer: Tracer
    private lateinit var span: Span

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        tracer = mock(Tracer::class.java)
        span = mock(Span::class.java)

        // Basic span setup for tracing
        `when`(tracer.currentSpan()).thenReturn(span)
        `when`(tracer.nextSpan()).thenReturn(span)
        `when`(span.name(any())).thenReturn(span)
        `when`(span.start()).thenReturn(span)

        userService = UserService(userRepository, passwordEncoder, tracer)
    }

    @Test
    fun `encodePassword should call passwordEncoder`() {
        // Given
        val rawPassword = "password123"
        val encodedPassword = "encodedPassword"
        `when`(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword)

        // When
        val result = userService.encodePassword(rawPassword)

        // Then
        verify(passwordEncoder).encode(rawPassword)
        assert(result == encodedPassword)
    }

    @Test
    fun `validatePassword should call passwordEncoder matches`() {
        // Given
        val rawPassword = "password123"
        val encodedPassword = "encodedPassword"
        `when`(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true)

        // When
        val result = userService.validatePassword(rawPassword, encodedPassword)

        // Then
        verify(passwordEncoder).matches(rawPassword, encodedPassword)
        assert(result)
    }

    @Test
    fun `getAllUsers should return list of non-deleted users`() {
        // Given
        val users = listOf(
            User(id = 1, name = "User 1", email = "user1@example.com", password = "encoded1"),
            User(id = 2, name = "User 2", email = "user2@example.com", password = "encoded2")
        )
        `when`(userRepository.findAllByDeletedFalse()).thenReturn(users)

        // When
        val result = userService.getAllUsers()

        // Then
        verify(userRepository).findAllByDeletedFalse()
        assert(result.size == 2)
        assert(result[0].name == "User 1")
        assert(result[0].email == "user1@example.com")
        assert(result[1].name == "User 2")
        assert(result[1].email == "user2@example.com")
    }

    @Test
    fun `getUserById should return user when found`() {
        // Given
        val userId = 1L
        val user = User(id = userId, name = "Test User", email = "test@example.com", password = "encoded")
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // When
        val result = userService.getUserById(userId)

        // Then
        verify(userRepository).findById(userId)
        assert(result.name == "Test User")
        assert(result.email == "test@example.com")
    }

    @Test
    fun `getUserById should throw exception when user not found`() {
        // Given
        val userId = 1L
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        // When/Then
        assertThrows<RuntimeException> { userService.getUserById(userId) }
        verify(userRepository).findById(userId)
    }

    @Test
    fun `createUser should save and return new user`() {
        // Given
        val createRequest = CreateUserRequest("New User", "new@example.com", "password123")
        val encodedPassword = "encodedPassword"
        val savedUser = User(
            id = 1,
            name = createRequest.name,
            email = createRequest.email,
            password = encodedPassword
        )

        `when`(passwordEncoder.encode(createRequest.password)).thenReturn(encodedPassword)
        `when`(userRepository.save(any())).thenReturn(savedUser)

        // When
        val result = userService.createUser(createRequest)

        // Then
        verify(passwordEncoder).encode(createRequest.password)
        verify(userRepository).save(any())
        assert(result.name == createRequest.name)
        assert(result.email == createRequest.email)
    }

    @Test
    fun `updateUser should update and return user when password is valid`() {
        // Given
        val email = "existing@example.com"
        val updateRequest = UpdateUserRequest("Updated Name", "password123")
        val existingUser = User(
            id = 1,
            name = "Existing User",
            email = email,
            password = "encodedPassword"
        )
        val updatedUser = existingUser.copy(name = updateRequest.name)

        `when`(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser))
        `when`(passwordEncoder.matches(updateRequest.password, existingUser.password)).thenReturn(true)
        `when`(userRepository.save(any())).thenReturn(updatedUser)

        // When
        val result = userService.updateUser(email, updateRequest)

        // Then
        verify(userRepository).findByEmail(email)
        verify(passwordEncoder).matches(updateRequest.password, existingUser.password)
        verify(userRepository).save(any())
        assert(result.name == updateRequest.name)
        assert(result.email == email)
    }

    @Test
    fun `updateUser should throw exception when password is invalid`() {
        // Given
        val email = "existing@example.com"
        val updateRequest = UpdateUserRequest("Updated Name", "wrongPassword")
        val existingUser = User(
            id = 1,
            name = "Existing User",
            email = email,
            password = "encodedPassword"
        )

        `when`(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser))
        `when`(passwordEncoder.matches(updateRequest.password, existingUser.password)).thenReturn(false)

        // When/Then
        assertThrows<RuntimeException> { userService.updateUser(email, updateRequest) }
        verify(userRepository).findByEmail(email)
        verify(passwordEncoder).matches(updateRequest.password, existingUser.password)
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `deleteUser should soft delete user when password is valid`() {
        // Given
        val email = "delete@example.com"
        val deleteRequest = DeleteUserRequest("password123")
        val user = User(
            id = 1,
            name = "Delete User",
            email = email,
            password = "encodedPassword"
        )

        // Setup mocks
        doReturn(Optional.of(user)).`when`(userRepository).findByEmail(email)
        doReturn(true).`when`(passwordEncoder).matches(deleteRequest.password, user.password)
        doReturn(1).`when`(userRepository).softDeleteByEmail(eq(email), any(LocalDateTime::class.java))
        doReturn(span).`when`(span).tag(any(String::class.java), any(String::class.java))
        doReturn(span).`when`(span).start()
        doReturn(span).`when`(span).end()

        // When
        val result = userService.deleteUser(email, deleteRequest)

        // Then
        verify(userRepository).findByEmail(email)
        verify(passwordEncoder).matches(deleteRequest.password, user.password)
        verify(userRepository).softDeleteByEmail(eq(email), any())
        assert(result["message"] == "User deleted successfully")
        assert(result["email"] == email)
    }

    @Test
    fun `deleteUser should throw exception when password is invalid`() {
        // Given
        val email = "delete@example.com"
        val deleteRequest = DeleteUserRequest("wrongPassword")
        val user = User(
            id = 1,
            name = "Delete User",
            email = email,
            password = "encodedPassword"
        )

        // Setup mocks
        doReturn(Optional.of(user)).`when`(userRepository).findByEmail(email)
        doReturn(false).`when`(passwordEncoder).matches(deleteRequest.password, user.password)
        doReturn(span).`when`(span).tag(any(String::class.java), any(String::class.java))
        doReturn(span).`when`(span).start()
        doReturn(span).`when`(span).end()

        // When/Then
        val exception = assertThrows<RuntimeException> { userService.deleteUser(email, deleteRequest) }
        assert(exception.message?.contains("Password does not match") == true)

        // Verify
        verify(userRepository).findByEmail(email)
        verify(passwordEncoder).matches(deleteRequest.password, user.password)
        verify(userRepository, never()).softDeleteByEmail(any(), any())
    }
}

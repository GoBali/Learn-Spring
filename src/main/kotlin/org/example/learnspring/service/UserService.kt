package org.example.learnspring.service

import io.micrometer.tracing.Tracer
import mu.KotlinLogging
import org.example.learnspring.dto.CreateUserRequest
import org.example.learnspring.dto.DeleteUserRequest
import org.example.learnspring.dto.UpdateUserRequest
import org.example.learnspring.dto.UserDto
import org.example.learnspring.entity.User
import org.example.learnspring.repository.UserRepository
import org.example.learnspring.utility.withSpan
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tracer: Tracer
) {
    private val logger = KotlinLogging.logger {}

    fun encodePassword(rawPassword: String): String {
        return passwordEncoder.encode(rawPassword)
    }

    fun validatePassword(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }

    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserDto> {
        return userRepository.findAllByDeletedFalse().map { it.toDto() }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["user"], key = "#id")
    fun getUserById(id: Long): UserDto {
        val user = userRepository
            .findById(id)
            .orElseThrow { RuntimeException("User not found by ID: $id") }

        return user.toDto()
    }

    @Transactional
    fun createUser(createUserRequest: CreateUserRequest): UserDto {
        val savedUser = userRepository.save(User(
            name = createUserRequest.name,
            email = createUserRequest.email,
            password = encodePassword(createUserRequest.password)
        ))

        logger.info { "User created successfully: ${savedUser.email}" }
        return savedUser.toDto()
    }

    @Transactional
    @CachePut(value = ["user"], key = "#email")
    fun updateUser(email: String, updateUserRequest: UpdateUserRequest): UserDto {
        val existingUser = userRepository.findByEmail(email)
            .orElseThrow { RuntimeException("User not found by Email: ${email}") }

        if (!validatePassword(updateUserRequest.password, existingUser.password)) {
            throw RuntimeException("Password does not match")
        }

        val updatedUser = existingUser.copy(
            name = updateUserRequest.name,
        )

        logger.info { "User updated successfully from ${existingUser.email} to ${updatedUser.email}" }

        return userRepository.save(updatedUser).toDto()
    }

    @Transactional
    @CacheEvict(value = ["user"], key = "#email")
    fun deleteUser(email: String, deleteUserRequest: DeleteUserRequest): Map<String, String> {
        return tracer.withSpan("delete user") { span ->
            val user = tracer.withSpan(span, "find user") { userSpan ->
                userSpan.tag("user.email", email)
                userRepository.findByEmail(email).orElseThrow {
                    RuntimeException("User not found by Email: $email").also {
                        userSpan.tag("error", it.message ?: "Unknown error")
                    }
                }
            }

            tracer.withSpan(span, "validate password") { pwdSpan ->
                val isValid = validatePassword(deleteUserRequest.password, user.password)
                if (!isValid) {
                    val exception = RuntimeException("Password does not match")
                    pwdSpan.tag("error", exception.message ?: "Validation failed")
                    throw exception
                }
            }

            tracer.withSpan(span, "soft delete") { deleteSpan ->
                val deletionTime = LocalDateTime.now()
                userRepository.softDeleteByEmail(email, deletionTime)
                deleteSpan.tag("user.email", email)
            }

            tracer.withSpan(span, "logging") { logSpan ->
                logger.info { "User deleted successfully: ${user.email}" }
            }
            mapOf("message" to "User deleted successfully", "email" to email)
        }
    }

    private fun User.toDto(): UserDto = UserDto(
        name = this.name,
        email = this.email
    )
}



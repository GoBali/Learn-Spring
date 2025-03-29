package org.example.learnspring.service

import mu.KotlinLogging
import org.example.learnspring.config.SecurityConfig
import org.example.learnspring.dto.CreateUserRequest
import org.example.learnspring.dto.DeleteUserRequest
import org.example.learnspring.dto.UpdateUserRequest
import org.example.learnspring.dto.UserDto
import org.example.learnspring.entity.User
import org.example.learnspring.repository.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val securityConfig: SecurityConfig
) {
    private val logger = KotlinLogging.logger {}

    fun encodePassword(rawPassword: String): String {
        return securityConfig.passwordEncoder().encode(rawPassword)
    }

    fun validatePassword(rawPassword: String, encodedPassword: String): Boolean {
        return securityConfig.passwordEncoder().matches(rawPassword, encodedPassword)
    }

    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map { it.toDto() }
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

        securityConfig.passwordEncoder()
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
        val user = userRepository.findByEmail(email)
            .orElseThrow { RuntimeException("User not found by Email: ${email}") }

        if (!validatePassword(deleteUserRequest.password, user.password))
        {
            throw RuntimeException("Password does not match")
        }

        userRepository.delete(user)

        logger.info { "User deleted successfully: ${user.email}" }

        return mapOf("message" to "User deleted successfully", "email" to email)
    }

    private fun User.toDto(): UserDto = UserDto(
        name = this.name,
        email = this.email
    )
}



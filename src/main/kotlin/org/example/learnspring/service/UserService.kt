package org.example.learnspring.service

import mu.KotlinLogging
import org.example.learnspring.dto.UserDto
import org.example.learnspring.entity.User
import org.example.learnspring.repository.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {
    private val logger = KotlinLogging.logger {}

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
    fun createUser(userDto: UserDto): UserDto {
        val user = userDto.toEntity()
        val savedUser = userRepository.save(user)

        logger.info { "User created successfully: ${savedUser.email}" }
        return savedUser.toDto()
    }

    @Transactional
    @CachePut(value = ["user"], key = "#email")
    fun updateUser(email: String, userDto: UserDto): UserDto {
        val existingUser = userRepository.findByEmail(email)
            .orElseThrow { RuntimeException("User not found by Email: $email") }

        val updatedUser = existingUser.copy(
            name = userDto.name,
            email = userDto.email
        )

        logger.info { "User updated successfully from ${existingUser.email} to ${updatedUser.email}" }

        return userRepository.save(updatedUser).toDto()
    }

    @Transactional
    @CacheEvict(value = ["user"], key = "#email")
    fun deleteUser(email: String): Map<String, String> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { RuntimeException("User not found by Email: $email") }

        userRepository.delete(user)

        logger.info { "User deleted successfully: ${user.email}" }

        return mapOf("message" to "User deleted successfully", "email" to email)
    }

    private fun User.toDto(): UserDto = UserDto(
        name = this.name,
        email = this.email
    )

    private fun UserDto.toEntity(): User = User(
        name = this.name,
        email = this.email
    )
}



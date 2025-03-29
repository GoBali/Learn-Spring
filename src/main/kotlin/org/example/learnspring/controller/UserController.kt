package org.example.learnspring.controller

import mu.KotlinLogging
import org.example.learnspring.dto.UserDto
import org.example.learnspring.entity.User
import org.example.learnspring.repository.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.web.bind.annotation.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/users")
class UserController(private val userRepository: UserRepository) {

    private val logger = KotlinLogging.logger {}

    private fun User.toDto(): UserDto = UserDto(
        name = this.name,
        email = this.email
    )

    private fun UserDto.toEntity(): User = User(
        name = this.name,
        email = this.email
    )

    @Cacheable("users")
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserDto>> {
        val userDtos = userRepository
            .findAll()
            .map { it.toDto() }
        return ResponseEntity.ok(userDtos)
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserDto> {
        return try {
            val userDto = userRepository
                .findById(id)
                .orElseThrow { RuntimeException("User not found!") }
                .toDto()
            ResponseEntity.ok(userDto)
        } catch (ex: RuntimeException) {
            logger.error("User not found: ${ex.message}")
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createUser(@RequestBody userDto: UserDto): ResponseEntity<Any> {
        return try {
            val savedUser = userRepository.save(userDto.toEntity())
            ResponseEntity.ok(savedUser.toDto())
        } catch (ex: Exception) {
            logger.error("Create user failed: ${ex.message}")
            ResponseEntity.badRequest().body(mapOf("message" to "Create user failed"))
        }
    }

    @Caching(
        evict = [CacheEvict(value = ["users"], key = "#email")],
        put = [CachePut(value = ["users"], key = "#email")]
    )
    @PutMapping("/{email}")
    fun updateUser(@PathVariable email: String, @RequestBody userDto: UserDto): ResponseEntity<UserDto> {
        return try {
            val existingUser = userRepository
                .findByEmail(email)
                .orElseThrow({ RuntimeException("User not found!") } )

            val updatedUser = existingUser.copy(
                name = userDto.name,
                email = userDto.email
            )

             ResponseEntity.ok(userRepository.save(updatedUser).toDto())
        } catch (ex: RuntimeException) {
            logger.error("Update user failed: ${ex.message}")
            ResponseEntity.badRequest().body(UserDto("Update user failed", ""))
        }
    }

    @CacheEvict(value = ["users"], key = "#email")
    @DeleteMapping("/{email}")
    fun deleteUser(@PathVariable email: String): ResponseEntity<Map<String, String>> {
        return try {
            val user = userRepository
                .findByEmail(email)
                .orElseThrow({ RuntimeException("User not found for with email: $email") } )

            userRepository.delete(user)

            ResponseEntity.ok(
                mapOf(
                    "message" to "User successfully deleted",
                    "email" to email
                )
            )
        } catch (ex: Exception) {
            logger.error("User not found for with email: $email")
            ResponseEntity.badRequest().body(mapOf("message" to "User not found for with email: $email"))
        }
    }
}
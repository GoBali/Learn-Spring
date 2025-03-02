package org.example.learnspring.controller

import org.example.learnspring.entity.User
import org.example.learnspring.repository.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.web.bind.annotation.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching

@RestController
@RequestMapping("/api/users")
class UserController(private val userRepository: UserRepository) {
    @Cacheable("users")
    @GetMapping
    fun getAllUsers(): List<User> =
        userRepository.findAll()

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): User =
        userRepository.findById(id).orElseThrow { RuntimeException("User not found!") }

    @PostMapping
    fun createUser(@RequestBody user: User): User =
        userRepository.save(user)

    @Caching(
        evict = [CacheEvict(value = ["users"], key = "#user.id")],
        put = [CachePut(value = ["users"], key = "#user.id")]
    )
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody user: User):User {
        val existingUser = userRepository
            .findById(id)
            .orElseThrow({ RuntimeException("User not found!") } )
        val updatedUser = existingUser
            .copy(name = user.name, email = user.email)
        return userRepository.save(updatedUser)
    }

    @CacheEvict(value = ["users"], key = "#id")
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) {
        val user = userRepository
            .findById(id)
            .orElseThrow({ RuntimeException("User not found!") } )
        userRepository.delete(user)
    }
}
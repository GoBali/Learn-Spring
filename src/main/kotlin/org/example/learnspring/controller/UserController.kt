package org.example.learnspring.controller

import org.example.learnspring.dto.UserDto
import org.example.learnspring.service.UserService
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getAllUsers() = ResponseEntity.ok(userService.getAllUsers())

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long) =
        ResponseEntity.ok(userService.getUserById(id))

    @PostMapping
    fun createUser(@RequestBody userDto: UserDto) =
        ResponseEntity.ok(userService.createUser(userDto))

    @PutMapping("/{email}")
    fun updateUser(@PathVariable email: String, @RequestBody userDto: UserDto) =
        ResponseEntity.ok(userService.updateUser(email, userDto))

    @DeleteMapping("/{email}")
    fun deleteUser(@PathVariable email: String) =
        ResponseEntity.ok(userService.deleteUser(email))
}
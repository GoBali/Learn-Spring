package org.example.learnspring.controller

import org.example.learnspring.dto.CreateUserRequest
import org.example.learnspring.dto.DeleteUserRequest
import org.example.learnspring.dto.UpdateUserRequest
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
    fun createUser(@RequestBody createUserRequest: CreateUserRequest) =
        ResponseEntity.ok(userService.createUser(createUserRequest))

    @PutMapping("/{email}")
    fun updateUser(@PathVariable email: String, @RequestBody updateUserRequest: UpdateUserRequest) =
        ResponseEntity.ok(userService.updateUser(email, updateUserRequest))

    @DeleteMapping("/{email}")
    fun deleteUser(@PathVariable email: String, deleteUserRequest: DeleteUserRequest) =
        ResponseEntity.ok(userService.deleteUser(email, deleteUserRequest))
}
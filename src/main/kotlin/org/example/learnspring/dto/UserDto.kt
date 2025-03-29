package org.example.learnspring.dto

data class UserDto(
    val name: String,
    val email: String
)

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String
)

data class UpdateUserRequest(
    val name: String,
    val password: String
)

data class DeleteUserRequest(
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)
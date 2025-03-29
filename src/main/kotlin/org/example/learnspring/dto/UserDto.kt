package org.example.learnspring.dto

data class UserDto(
    val name: String,
    val email: String
)

data class LoginRequest(
    val username: String,
    val password: String
)
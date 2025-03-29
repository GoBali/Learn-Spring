package org.example.learnspring.service

import org.example.learnspring.dto.LoginRequest
import org.example.learnspring.exception.AuthenticationException
import org.example.learnspring.security.JwtTokenProvider
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AuthService(private val jwtTokenProvider: JwtTokenProvider) {
    private fun isValidUser(username: String, password: String): Boolean {
        return true // TODO: DB 검증 필요
    }

    fun login(loginRequest: LoginRequest): ResponseEntity<Map<String, String>> {
        try {
            if (isValidUser(loginRequest.username, loginRequest.password)) {
                val roles = listOf("USER")
                val token = jwtTokenProvider.createToken(loginRequest.username, roles)

                val authorities = roles.map {
                    SimpleGrantedAuthority("ROLE_$it")
                }
                val auth: Authentication =
                    UsernamePasswordAuthenticationToken(
                        loginRequest.username, null, authorities
                    )
                SecurityContextHolder.getContext().authentication = auth

                return ResponseEntity.ok(mapOf(
                    "token" to token,
                    "redirectUrl" to "/swagger-ui/index.html"
                ))
            } else {
                throw AuthenticationException("Invalid username or password")
            }
        } catch (e: Exception) {
            throw AuthenticationException(e.message ?: "Authentication failed!")
        }
    }
}
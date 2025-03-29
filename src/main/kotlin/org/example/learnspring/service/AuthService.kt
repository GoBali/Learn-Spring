package org.example.learnspring.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.example.learnspring.config.SecurityConfig
import org.example.learnspring.dto.LoginRequest
import org.example.learnspring.exception.AuthenticationException
import org.example.learnspring.repository.UserRepository
import org.example.learnspring.security.JwtTokenProvider
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtTokenProvider: JwtTokenProvider,
    meterRegistry: MeterRegistry,
    private val userRepository: UserRepository,
    private val securityConfig: SecurityConfig
) {

    private val loginSuccessCounter = Counter.builder("login.success.count").register(meterRegistry)
    private val loginFailureCounter = Counter.builder("login.failure.count").register(meterRegistry)

    private fun isValidUser(email: String, password: String): Boolean {
        val user = userRepository.findByEmail(email)
        return user.isPresent && securityConfig.passwordEncoder().matches(password, user.get().password)
    }

    fun login(loginRequest: LoginRequest): ResponseEntity<Map<String, String>> {
        try {
            if (isValidUser(loginRequest.email, loginRequest.password)) {
                val roles = listOf("USER")
                val token = jwtTokenProvider.createToken(loginRequest.email, roles)

                val authorities = roles.map {
                    SimpleGrantedAuthority("ROLE_$it")
                }
                val auth: Authentication =
                    UsernamePasswordAuthenticationToken(
                        loginRequest.email, null, authorities
                    )
                SecurityContextHolder.getContext().authentication = auth

                loginSuccessCounter.increment()

                return ResponseEntity.ok(mapOf(
                    "token" to token,
                    "redirectUrl" to "/swagger-ui/index.html"
                ))
            } else {
                loginFailureCounter.increment()

                throw AuthenticationException("Invalid username or password")
            }
        } catch (e: Exception) {
            loginFailureCounter.increment()

            throw AuthenticationException(e.message ?: "Authentication failed!")
        }
    }
}
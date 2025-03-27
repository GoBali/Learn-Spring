package org.example.learnspring.controller

import org.example.learnspring.security.JwtTokenProvider
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class AuthController {

    @GetMapping("/login")
    fun loginPage(): String {
        return "login"
    }

    @GetMapping("/")
    fun home(): String {
        return "redirect:/dashboard"
    }

    @GetMapping("/dashboard")
    fun dashboard(): String {
        return "dashboard"
    }
}

@RestController
@RequestMapping("/api/auth")
class AuthApiController(
    private val jwtTokenProvider: JwtTokenProvider,
) {
    private fun isValidUser(username: String, password: String): Boolean {
        return true // TODO: DB 검증 필요
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<*> {
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
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid username or password"))
            }
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Authentication failed!"))
        }
    }
}

data class LoginRequest(
    val username: String,
    val password: String
)

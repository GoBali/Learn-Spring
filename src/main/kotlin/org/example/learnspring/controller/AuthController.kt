package org.example.learnspring.controller

import com.fasterxml.jackson.databind.ObjectMapper
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
        return "login"  // login.html 템플릿을 반환합니다
    }

    @GetMapping("/")
    fun home(): String {
        return "redirect:/dashboard"  // 로그인 성공 시 대시보드로 리다이렉트
    }

    @GetMapping("/dashboard")
    fun dashboard(): String {
        return "dashboard"  // dashboard.html 템플릿을 반환합니다
    }
}

@RestController
@RequestMapping("/api/auth")
class AuthApiController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val objectMapper: ObjectMapper,
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

                return ResponseEntity.ok(mapOf("token" to token))
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid username or password"))
            }
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Authentication failed!"))
        }
    }
}

// 로그인 요청 데이터 클래스
data class LoginRequest(
    val username: String,
    val password: String
)

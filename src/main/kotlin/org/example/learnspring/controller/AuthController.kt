package org.example.learnspring.controller

import org.example.learnspring.dto.LoginRequest
import org.example.learnspring.service.AuthService
import org.springframework.http.ResponseEntity
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
    private val authService: AuthService,
) {
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<*> {
        return authService.login(loginRequest)
    }
}

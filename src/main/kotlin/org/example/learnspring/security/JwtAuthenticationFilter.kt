package org.example.learnspring.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    private fun isValidUser(username: String, password: String): Boolean {
        return true // TODO: DB 검증 필요
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.requestURI == "/api/auth/login" && request.method == "POST") {
            try {
                val loginRequest = objectMapper.readValue(request.inputStream, LoginRequest::class.java)

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

                    response.contentType = "application/json"
                    response.status = HttpServletResponse.SC_OK
                    response.writer.write("{\"token\":\"$token\"}")
                } else {
                    response.contentType = "application/json"
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.writer.write("{\"error\":\"Invalid username or password\"}")
                    return
                }
            } catch (e: Exception) {
                response.contentType = "application/json"
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.writer.write("{\"error\":\"${e.message}\"}")
                response.writer.flush()
                return
            }
        }

        filterChain.doFilter(request, response)
    }
}

data class LoginRequest(
    val username: String = "",
    val password: String = ""
)

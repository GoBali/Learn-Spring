package org.example.learnspring.config

import org.example.learnspring.security.JwtAuthorizationFilter
import org.example.learnspring.security.JwtTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Value("\${spring.security.filter.jwt.enabled:true}")
    private val jwtEnabled: Boolean = true

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF 설정
            .csrf { csrf ->
                csrf.disable() // JWT 기반 인증에서는 일반적으로 CSRF 보호를 비활성화합니다
            }
            // 세션 관리 - JWT 기반 인증에서는 STATELESS
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // 기본 HTTP 인증 비활성화 (JWT 인증 사용)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            // 로그아웃 설정
            .logout { logout ->
                logout
                    .logoutUrl("/api/auth/logout")
                    .logoutSuccessHandler { _, response, _ ->
                        response.status = 200
                        response.contentType = "application/json"
                        response.writer.write("{\"message\":\"로그아웃 성공\"}")
                    }
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
            }
            // 헤더 보안 설정
            .headers { headers ->
                headers
                    .frameOptions { frameOptions ->
                        frameOptions.sameOrigin()
                    }
                    .xssProtection {}
                    .contentSecurityPolicy { csp ->
                        csp.policyDirectives(
                            "default-src 'self'; " +
                                    "script-src 'self' 'unsafe-inline'; " +
                                    "style-src 'self' 'unsafe-inline'; " +
                                    "img-src 'self' data:; " +
                                    "connect-src 'self'; " +
                                    "frame-ancestors 'self'; " +
                                    "form-action 'self'; " +
                                    "base-uri 'self';"
                        )
                    }
                    .httpStrictTransportSecurity { hsts ->
                        hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000)
                    }
            }
            .requiresChannel { requiresChannel ->
                requiresChannel
                    .anyRequest().requiresSecure()
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { request, response, _ ->
                        if (request.requestURI.startsWith("/api/")) {
                            response.status = 401
                            response.contentType = "application/json"
                            response.writer.write("{\"error\":\"Unauthorized\"}")
                        } else {
                            response.sendRedirect("/login")
                        }
                    }
                    .accessDeniedHandler { _, response, _ ->
                        response.status = 403
                        response.contentType = "application/json"
                        response.writer.write("{\"error\":\"접근 권한이 없습니다.\"}")
                    }
            }

        if (jwtEnabled) {
            val jwtAuthorizationFilter = JwtAuthorizationFilter(jwtTokenProvider)

            http.authorizeHttpRequests { auth ->
                    auth
                        .requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-resources/**",
                            "/api-docs/**",
                            "/api/auth/**", "/login",
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                }
            http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter::class.java)
        }
        else
        {
            http.authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
        }

        return http.build()
    }
}
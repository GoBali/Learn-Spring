package org.example.learnspring.config

import org.example.learnspring.security.JwtAuthenticationFilter
import org.example.learnspring.security.JwtAuthorizationFilter
import org.example.learnspring.security.JwtTokenProvider
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

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val jwtAuthorizationFilter = JwtAuthorizationFilter(jwtTokenProvider)

        http
            // CSRF 설정
            .csrf { csrf ->
                csrf.disable() // JWT 기반 인증에서는 일반적으로 CSRF 보호를 비활성화합니다
            }
            // 요청 인가 설정
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/swagger-ui/**",   // Swagger UI 관련 허용
                        "/v3/api-docs/**",  // OpenAPI 문서 관련 허용
                        "/swagger-resources/**",  // Swagger 리소스 허용
                        "/api/auth/**"      // 인증 관련 API (로그인, 회원가입 등)
                    ).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight 요청 허용
                    .anyRequest().authenticated() // 나머지 요청은 인증 필요
            }
            // 세션 관리 - JWT 기반 인증에서는 STATELESS
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // 폼 로그인 비활성화 (JWT 인증 사용)
            .formLogin { formLogin ->
                formLogin.disable()
            }
            // 기본 HTTP 인증 비활성화 (JWT 인증 사용)
            .httpBasic { httpBasic ->
                httpBasic.disable()
            }
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
            // HTTPS 요구 설정
            .requiresChannel { requiresChannel ->
                requiresChannel
                    .anyRequest().requiresSecure()
            }
            // 예외 처리
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { _, response, _ ->
                        response.status = 401
                        response.contentType = "application/json"
                        response.writer.write("{\"error\":\"인증되지 않은 사용자입니다.\"}")
                    }
                    .accessDeniedHandler { _, response, _ ->
                        response.status = 403
                        response.contentType = "application/json"
                        response.writer.write("{\"error\":\"접근 권한이 없습니다.\"}")
                    }
            }
            // JWT 필터 추가
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
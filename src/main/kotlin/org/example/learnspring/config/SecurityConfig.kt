package org.example.learnspring.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/swagger-ui/**",   // Swagger UI 관련 허용
                        "/v3/api-docs/**",  // OpenAPI 문서 관련 허용
                        "/swagger-resources/**"  // Swagger 리소스 허용
                    ).permitAll() // 누구나 접근 가능하도록 설정
                    .anyRequest().authenticated() // 나머지 요청은 인증 필요
            }
        return http.build()
    }
}
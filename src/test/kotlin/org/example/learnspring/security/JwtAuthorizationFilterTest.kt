package org.example.learnspring.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthorizationFilterTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtAuthorizationFilter: JwtAuthorizationFilter
    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse
    private lateinit var filterChain: FilterChain
    private lateinit var authentication: Authentication

    @BeforeEach
    fun setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext()

        // Create mocks
        jwtTokenProvider = mock(JwtTokenProvider::class.java)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
        filterChain = mock(FilterChain::class.java)
        authentication = mock(Authentication::class.java)

        // Create filter with mock token provider
        jwtAuthorizationFilter = JwtAuthorizationFilter(jwtTokenProvider)
    }

    @Test
    fun `doFilter should set authentication when token is valid`() {
        // Given
        val token = "valid.jwt.token"
        request.addHeader("Authorization", "Bearer $token")
        `when`(jwtTokenProvider.validateToken(token)).thenReturn(true)
        `when`(jwtTokenProvider.getAuthentication(token)).thenReturn(authentication)

        // When
        jwtAuthorizationFilter.doFilter(request, response, filterChain)

        // Then
        verify(jwtTokenProvider).validateToken(token)
        verify(jwtTokenProvider).getAuthentication(token)
        verify(filterChain).doFilter(request, response)
        assert(SecurityContextHolder.getContext().authentication === authentication)
    }

    @Test
    fun `doFilter should not set authentication when token is invalid`() {
        // Given
        val token = "invalid.jwt.token"
        request.addHeader("Authorization", "Bearer $token")
        `when`(jwtTokenProvider.validateToken(token)).thenReturn(false)

        // When
        jwtAuthorizationFilter.doFilter(request, response, filterChain)

        // Then
        verify(jwtTokenProvider).validateToken(token)
        verify(jwtTokenProvider, never()).getAuthentication(token)
        verify(filterChain).doFilter(request, response)
        assert(SecurityContextHolder.getContext().authentication == null)
    }

    @Test
    fun `doFilter should not set authentication when Authorization header is missing`() {
        // Given
        // No Authorization header

        // When
        jwtAuthorizationFilter.doFilter(request, response, filterChain)

        // Then
        verify(jwtTokenProvider, never()).validateToken(anyString())
        verify(jwtTokenProvider, never()).getAuthentication(anyString())
        verify(filterChain).doFilter(request, response)
        assert(SecurityContextHolder.getContext().authentication == null)
    }

    @Test
    fun `doFilter should not set authentication when Authorization header does not start with Bearer`() {
        // Given
        request.addHeader("Authorization", "Token some-token")

        // When
        jwtAuthorizationFilter.doFilter(request, response, filterChain)

        // Then
        verify(jwtTokenProvider, never()).validateToken(anyString())
        verify(jwtTokenProvider, never()).getAuthentication(anyString())
        verify(filterChain).doFilter(request, response)
        assert(SecurityContextHolder.getContext().authentication == null)
    }

    @Test
    fun `doFilter should handle exceptions and clear security context`() {
        // Given
        val token = "exception.token"
        request.addHeader("Authorization", "Bearer $token")
        `when`(jwtTokenProvider.validateToken(token)).thenReturn(true)
        `when`(jwtTokenProvider.getAuthentication(token)).thenThrow(RuntimeException("Token error"))

        // When
        jwtAuthorizationFilter.doFilter(request, response, filterChain)

        // Then
        verify(jwtTokenProvider).validateToken(token)
        verify(jwtTokenProvider).getAuthentication(token)
        assert(response.status == HttpServletResponse.SC_UNAUTHORIZED)
        verify(filterChain).doFilter(request, response)
        assert(SecurityContextHolder.getContext().authentication == null)
    }

    @Test
    fun `doFilter should skip filter for login endpoint with POST method`() {
        // Given
        request.requestURI = "/api/auth/login"
        request.method = "POST"

        // When
        jwtAuthorizationFilter.doFilter(request, response, filterChain)

        // Then
        verify(jwtTokenProvider, never()).validateToken(anyString())
        verify(jwtTokenProvider, never()).getAuthentication(anyString())
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `doFilter should not skip filter for other endpoints`() {
        // Given
        request.requestURI = "/api/users"
        request.method = "GET"

        // When
        jwtAuthorizationFilter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
        // We can't verify more without a token, but at least we know the filter chain was called
    }
}

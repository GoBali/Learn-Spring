package org.example.learnspring.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.util.ReflectionTestUtils
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private val secretKey = "thisIsATestSecretKeyForJwtTokenProviderTestingPurposes"
    private val validityInMilliseconds = 3600000L

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = JwtTokenProvider()
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKeyString", secretKey)
        ReflectionTestUtils.setField(jwtTokenProvider, "validityInMilliseconds", validityInMilliseconds)
    }

    @Test
    fun `createToken should generate valid JWT token`() {
        // Given
        val email = "test@example.com"
        val roles = listOf("USER")

        // When
        val token = jwtTokenProvider.createToken(email, roles)

        // Then
        assertTrue(token.isNotEmpty())
        assertTrue(jwtTokenProvider.validateToken(token))
    }

    @Test
    fun `validateToken should return true for valid token`() {
        // Given
        val email = "test@example.com"
        val roles = listOf("USER")
        val token = jwtTokenProvider.createToken(email, roles)

        // When
        val isValid = jwtTokenProvider.validateToken(token)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `validateToken should return false for expired token`() {
        // Given
        val email = "test@example.com"
        val roles = listOf("USER")
        
        // Create a token that's already expired
        val key = Keys.hmacShaKeyFor(secretKey.toByteArray())
        val now = Date()
        val expiration = Date(now.time - 1000) // Expired 1 second ago
        
        val claims = Jwts.claims().setSubject(email)
        claims["roles"] = roles
        
        val token = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(key)
            .compact()

        // When
        val isValid = jwtTokenProvider.validateToken(token)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `validateToken should return false for invalid token`() {
        // Given
        val invalidToken = "invalid.token.string"

        // When
        val isValid = jwtTokenProvider.validateToken(invalidToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `getAuthentication should return Authentication with correct principal and authorities`() {
        // Given
        val email = "test@example.com"
        val roles = listOf("USER", "ADMIN")
        val token = jwtTokenProvider.createToken(email, roles)

        // When
        val authentication = jwtTokenProvider.getAuthentication(token)

        // Then
        assertEquals(email, authentication.name)
        assertEquals(2, authentication.authorities.size)
        assertTrue(authentication.authorities.contains(SimpleGrantedAuthority("ROLE_USER")))
        assertTrue(authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN")))
    }

    @Test
    fun `getAuthentication should throw exception for invalid token`() {
        // Given
        val invalidToken = "invalid.token.string"

        // When/Then
        assertThrows<Exception> { jwtTokenProvider.getAuthentication(invalidToken) }
    }
}
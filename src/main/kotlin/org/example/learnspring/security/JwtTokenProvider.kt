package org.example.learnspring.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider {
    @Value("\${jwt.secret}")
    private lateinit var secretKeyString: String

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKeyString.toByteArray())
    }

    private val jwtParser: JwtParser by lazy {
        Jwts.parserBuilder().setSigningKey(secretKey).build()
    }

    @Value("\${jwt.expiration}")
    private var validityInMilliseconds: Long = 3600000

    fun createToken(email: String, roles: List<String>): String {
        val claims: Claims = Jwts.claims().setSubject(email)
        claims["roles"] = roles

        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims = jwtParser
            .parseClaimsJws(token)
            .body

        @Suppress("UNCHECKED_CAST")
        val authorities: Collection<GrantedAuthority> =
            (claims["roles"] as List<String>)
                .map { SimpleGrantedAuthority("ROLE_$it") }

        val principal = User(claims.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims = jwtParser.parseClaimsJws(token)
            return !claims.body.expiration.before(Date())
        } catch (e: Exception) {
            return false
        }
    }

//    fun getEmail(token: String): String {
//        return jwtParser
//            .parseClaimsJws(token)
//            .body
//            .subject
//    }
}
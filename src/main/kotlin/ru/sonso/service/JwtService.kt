package ru.sonso.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import ru.sonso.entity.AdminEntity
import ru.sonso.properties.AuthenticationProperties
import java.util.Date

@Service
class JwtService(
    private val authenticationProperties: AuthenticationProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val signKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(authenticationProperties.secret))

    fun generateTokens(userDetails: AdminEntity) = Pair(
        generateAccessToken(userDetails),
        generateRefreshToken(userDetails),
    )

    private fun generateAccessToken(user: AdminEntity): String {
        logger.debug("Generating access token for {}", user.email)
        return Jwts.builder()
            .subject(user.email)
            .claim("role", user.role.name)
            .expiration(Date(System.currentTimeMillis() + authenticationProperties.accessLifeTime))
            .signWith(signKey)
            .compact()
    }

    private fun generateRefreshToken(user: AdminEntity): String = Jwts.builder()
        .subject(user.email)
        .claim("role", user.role.name)
        .expiration(Date(System.currentTimeMillis() + authenticationProperties.refreshLifeTime))
        .signWith(signKey)
        .compact()

    private fun isTokenExpired(token: String): Boolean = extractClaims(token).expiration.before(Date())

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractClaims(token).subject
        return username == userDetails.username && !isTokenExpired(token)
    }

    fun getLogin(token: String): String = extractClaims(token).subject
        .also { logger.debug("Resolved login from token") }

    private fun extractClaims(token: String): Claims = Jwts
        .parser()
        .verifyWith(signKey)
        .build()
        .parseSignedClaims(token)
        .payload
}

package ru.sonso.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import ru.sonso.entity.AdminEntity
import ru.sonso.properties.AuthenticationProperties
import java.util.*

@Service
class JwtService(
    private val authenticationProperties: AuthenticationProperties,
) {
    private val signKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(authenticationProperties.secret))

    // Генератор токенов
    fun generateTokens(userDetails: AdminEntity) = Pair(
        generateAccessToken(userDetails),
        generateRefreshToken(userDetails)
    )

    private fun generateAccessToken(user: AdminEntity): String =
        Jwts.builder()
            .subject(user.email)
            .claim("role", user.role.name)
            .expiration(Date(System.currentTimeMillis() + authenticationProperties.accessLifeTime))
            .signWith(signKey)
            .compact()

    private fun generateRefreshToken(user: AdminEntity): String =
        Jwts.builder()
            .subject(user.email)
            .claim("role", user.role.name)
            .expiration(Date(System.currentTimeMillis() + authenticationProperties.refreshLifeTime))
            .signWith(signKey)
            .compact()

    // Валидаторы
    private fun isTokenExpired(token: String): Boolean = extractClaims(token).expiration.before(Date())

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractClaims(token).subject
        return username == userDetails.username && !isTokenExpired(token)
    }

    // Экстракторы
    fun getLogin(token: String): String = extractClaims(token).subject

    private fun extractClaims(token: String): Claims = Jwts
        .parser()
        .verifyWith(signKey)
        .build()
        .parseSignedClaims(token)
        .payload
}

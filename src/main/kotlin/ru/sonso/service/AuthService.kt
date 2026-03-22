package ru.sonso.service

import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.sonso.dto.request.LoginRequest
import ru.sonso.dto.response.LoginResponse
import ru.sonso.entity.AdminEntity
import ru.sonso.mapper.toAdminShort
import ru.sonso.repository.AdminRepository
import java.time.Duration

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val adminRepository: AdminRepository,
    private val jwtService: JwtService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun login(request: LoginRequest, response: HttpServletResponse): LoginResponse {
        val email = request.email.trim()
        val password = request.password
        logger.info("Authenticating admin, email={}", email)

        val authentication = try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(email, password)
            )
        } catch (_: BadCredentialsException) {
            logger.warn("Authentication failed, email={}", email)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        }

        val admin = (authentication.principal as? AdminEntity)
            ?: adminRepository.findByEmailIgnoreCase(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password")

        val (accessToken, refreshToken) = jwtService.generateTokens(admin)
        setRefreshToken(response, refreshToken)

        return LoginResponse(
            token = accessToken,
            admin = admin.toAdminShort(),
        ).also { logger.info("Authentication successful, email={}", email) }
    }

    fun refresh(token: String, response: HttpServletResponse): LoginResponse {
        if (token.isEmpty()) {
            logger.warn("Token is empty")
            throw IllegalArgumentException("Токен пуст")
        }

        val admin = checkNotNull(
            adminRepository.findByEmailIgnoreCase(jwtService.getLogin(token.substring(7)))
        ) {
            "User not found in database. Maybe he is not registered"
        }

        val (accessToken, refreshToken) = jwtService.generateTokens(admin)
        setRefreshToken(response, refreshToken)

        logger.debug("Token for user {} has been refreshed", admin.id)

        return LoginResponse(
            token = accessToken,
            admin = admin.toAdminShort(),
        ).also { logger.info("Token refresh successful, email={}", admin.email) }
    }

    private fun setRefreshToken(response: HttpServletResponse, token: String) {
        val cookie = ResponseCookie.from("refreshToken", "Bearer_$token")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(30))
            .sameSite("None")
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }
}

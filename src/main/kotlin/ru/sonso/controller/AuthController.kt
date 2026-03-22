package ru.sonso.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sonso.dto.request.LoginRequest
import ru.sonso.dto.response.LoginResponse
import ru.sonso.service.AuthService

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        httpResponse: HttpServletResponse,
    ): ResponseEntity<LoginResponse> {
        logger.info("POST /api/auth/login requested, email={}", request.email)
        val response = authService.login(request, httpResponse)
        logger.info("POST /api/auth/login completed")
        return ResponseEntity.ok(response)
    }

    @GetMapping("/refresh")
    @Operation(summary = "Обновление токена")
    fun refresh(
        @CookieValue(value = "refreshToken") token: String,
        httpResponse: HttpServletResponse,
    ): ResponseEntity<LoginResponse> {
        logger.info("GET /api/auth/refresh requested")
        val response = authService.refresh(token, httpResponse)
        logger.info("GET /api/auth/refresh completed")
        return ResponseEntity.ok(response)
    }
}

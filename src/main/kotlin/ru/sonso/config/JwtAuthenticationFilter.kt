package ru.sonso.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.sonso.service.JwtService

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

        try {
            if (request.requestURI.startsWith("/api/auth") && request.requestURI != "/api/auth/who-am-i") {
                filterChain.doFilter(request, response)
                return
            }

            val token = request.getHeader(HttpHeaders.AUTHORIZATION)
            logger.debug("authHeader = $token")

            if (token == null || token.isEmpty()) {
                logger.debug("Token is empty or null")
                filterChain.doFilter(request, response)
                return
            }

            if (!token.startsWith("Bearer ")) {
                logger.debug("Token is not started from 'Bearer '")
                return
            }

            val userPhoneNumber = jwtService.getLogin(token)

            if (userPhoneNumber.isNotEmpty() && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = userDetailsService.loadUserByUsername(userPhoneNumber)

                if (jwtService.isTokenValid(token, userDetails)) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails,
                        token,
                        userDetails.authorities
                    )

                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                } else {
                    logger.debug("Token is invalid")
                    return
                }
            }

            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            logger.warn("Token validation ended with exception: ${ex.message}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write(
                """
            {
                "status": 401,
                "error": "Unauthorized",
                "message": "Токен не валиден",
                "path": "${request.requestURI}"
            }
            """.trimIndent()
            )
        }
    }
}

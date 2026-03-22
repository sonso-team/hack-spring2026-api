package ru.sonso.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import ru.sonso.repository.AdminRepository
import ru.sonso.service.JwtService
import ru.sonso.util.CustomAccessDeniedHandler
import ru.sonso.util.CustomAuthenticationEntryPoint

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val adminRepository: AdminRepository,
    private val jwtService: JwtService,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
) {
    @Bean
    fun userDetailsService(): UserDetailsService = UserDetailsService { email ->
        adminRepository.findByEmailIgnoreCase(email)
            ?: throw UsernameNotFoundException("Invalid email or password")
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(userDetailsService: UserDetailsService): AuthenticationProvider =
        DaoAuthenticationProvider(userDetailsService).apply {
            setPasswordEncoder(passwordEncoder())
        }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    @Bean
    fun securityFilterChain(http: HttpSecurity, userDetailsService: UserDetailsService): SecurityFilterChain {
        http
            .cors {}
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(
                JwtAuthenticationFilter(userDetailsService, jwtService),
                UsernamePasswordAuthenticationFilter::class.java,
            )
            .exceptionHandling {
                it.authenticationEntryPoint(customAuthenticationEntryPoint)
                it.accessDeniedHandler(customAccessDeniedHandler)
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/auth/login",
                    "/api/play/**",
                    "/api/swagger",
                    "/api/swagger/**",
                    "/api/docs",
                    "/api/docs/**",
                    "/v3/api-docs",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                ).permitAll()
                    .requestMatchers("/api/admin/admins", "/api/admin/admins/**").hasAuthority("SUPERADMIN")
                    .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN", "SUPERADMIN")
                    .anyRequest().permitAll()
            }

        return http.build()
    }
}

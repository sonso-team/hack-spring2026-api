package ru.sonso.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.auth")
data class AuthenticationProperties(
    val accessLifeTime: Long,
    val refreshLifeTime: Long,
    val secret: String,
)

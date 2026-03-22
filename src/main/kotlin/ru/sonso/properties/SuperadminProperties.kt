package ru.sonso.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.superadmin")
data class SuperadminProperties(
    val firstName: String,
    val lastName: String,
    val position: String?,
    val email: String,
    val password: String,
)

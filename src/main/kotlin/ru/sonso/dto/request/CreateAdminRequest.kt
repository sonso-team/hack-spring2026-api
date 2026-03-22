package ru.sonso.dto.request

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateAdminRequest(
    val firstName: String,
    val lastName: String,
    val position: String?,
    val email: String,
    val password: String,
)

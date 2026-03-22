package ru.sonso.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AdminFull(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val position: String?,
    val email: String,
    val role: String,
)
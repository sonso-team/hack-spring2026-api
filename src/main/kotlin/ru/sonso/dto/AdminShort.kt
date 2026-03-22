package ru.sonso.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AdminShort(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val role: String,
)

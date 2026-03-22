package ru.sonso.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class StartGameResponse(
    val sessionToken: String,
    val difficulty: String?,
    val durationSeconds: Int,
)

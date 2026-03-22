package ru.sonso.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RandomWinner(
    val playerId: UUID,
    val firstName: String,
    val lastName: String,
    val score: Int,
)

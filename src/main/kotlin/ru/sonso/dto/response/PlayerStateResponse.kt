package ru.sonso.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PlayerStateResponse(
    val playerId: UUID?,
    val registered: Boolean,
    val canPlay: Boolean,
    val attemptsLeft: Int,
)

package ru.sonso.dto.request

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import ru.sonso.enumerable.LobbyDifficulty

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateLobbyRequest(
    val name: String,
    val game: String,
    val difficulty: String = LobbyDifficulty.MEDIUM.name,
    val durationMinutes: Int,
    val maxAttempts: Int,
    val gameOverText: String,
)

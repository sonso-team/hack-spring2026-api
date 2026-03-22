package ru.sonso.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.OffsetDateTime
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Lobby(
    val id: UUID,
    val name: String,
    val game: String,
    val difficulty: String?,
    val durationMinutes: Int,
    val maxAttempts: Int,
    val gameOverText: String,
    val status: String,
    val inviteCode: String,
    val playersCount: Int,
    val createdAt: OffsetDateTime,
    val closedAt: OffsetDateTime?,
)

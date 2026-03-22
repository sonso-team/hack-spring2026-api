package ru.sonso.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.OffsetDateTime
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PlayerResult(
    val rank: Int,
    val playerId: UUID,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val score: Int,
    val durationSeconds: Int,
    val status: String,
    val playedAt: OffsetDateTime,
)

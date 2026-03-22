package ru.sonso.mapper

import ru.sonso.dto.request.CreateLobbyRequest
import ru.sonso.dto.response.Lobby
import ru.sonso.entity.LobbyEntity
import ru.sonso.enumerable.LobbyStatus
import java.time.OffsetDateTime

fun CreateLobbyRequest.toLobbyEntity(inviteCode: String): LobbyEntity = LobbyEntity(
    name = name.trim(),
    game = game.trim(),
    difficulty = difficulty.toLobbyDifficulty(),
    durationMinutes = durationMinutes,
    maxAttempts = maxAttempts,
    gameOverText = gameOverText.trim(),
    status = LobbyStatus.ACTIVE,
    inviteCode = inviteCode,
    createdAt = OffsetDateTime.now(),
    closedAt = null,
)

fun LobbyEntity.toLobby(playersCount: Int): Lobby = Lobby(
    id = checkNotNull(id) { "Lobby id is missing" },
    name = name,
    game = game,
    difficulty = difficulty.toApiValue(),
    durationMinutes = durationMinutes,
    maxAttempts = maxAttempts,
    gameOverText = gameOverText,
    status = status.toApiValue(),
    inviteCode = inviteCode,
    playersCount = playersCount,
    createdAt = createdAt,
    closedAt = closedAt,
)

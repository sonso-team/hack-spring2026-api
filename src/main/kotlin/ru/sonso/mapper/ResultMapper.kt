package ru.sonso.mapper

import ru.sonso.dto.PlayerResult
import ru.sonso.entity.GameSessionEntity
import ru.sonso.entity.PlayerEntity

fun GameSessionEntity.toPlayerResult(player: PlayerEntity, rank: Int): PlayerResult = PlayerResult(
    rank = rank,
    playerId = checkNotNull(player.id) { "Player id is missing" },
    firstName = player.firstName,
    lastName = player.lastName,
    phone = player.phone,
    email = player.email,
    score = finalScore ?: 0,
    durationSeconds = durationSeconds ?: 0,
    status = status.toApiValue(),
    playedAt = finishedAt ?: startedAt,
)

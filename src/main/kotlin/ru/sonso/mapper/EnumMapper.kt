package ru.sonso.mapper

import ru.sonso.enumerable.AdminRole
import ru.sonso.enumerable.LobbyDifficulty
import ru.sonso.enumerable.LobbyStatus
import ru.sonso.enumerable.SessionStatus

fun AdminRole.toApiValue(): String = name.lowercase()

fun LobbyDifficulty.toApiValue(): String = name.lowercase()

fun LobbyStatus.toApiValue(): String = name.lowercase()

fun SessionStatus.toApiValue(): String = when (this) {
    SessionStatus.STARTED -> "started"
    SessionStatus.COMPLETED -> "completed"
    SessionStatus.SUSPICIOUS -> "suspicious"
}

fun String.toLobbyDifficulty(): LobbyDifficulty {
    val normalized = lowercase()
    return when (normalized) {
        "easy" -> LobbyDifficulty.EASY
        "medium" -> LobbyDifficulty.MEDIUM
        "hard" -> LobbyDifficulty.HARD
        else -> throw IllegalArgumentException("Invalid difficulty: $this")
    }
}

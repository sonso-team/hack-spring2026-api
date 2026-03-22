package ru.sonso.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ru.sonso.enumerable.LobbyDifficulty
import ru.sonso.enumerable.LobbyStatus
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "lobbies")
data class LobbyEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @field:Column(name = "name", nullable = false, length = 255)
    var name: String = "",

    @field:Column(name = "game", nullable = false, length = 50)
    var game: String = "",

    @field:Enumerated(EnumType.STRING)
    @field:JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @field:Column(name = "difficulty", columnDefinition = "LOBBY_DIFFICULTY")
    var difficulty: LobbyDifficulty = LobbyDifficulty.MEDIUM,

    @field:Column(name = "duration_minutes", nullable = false)
    var durationMinutes: Int = 0,

    @field:Column(name = "max_attempts", nullable = false)
    var maxAttempts: Int = 0,

    @field:Column(name = "game_over_text", nullable = false)
    var gameOverText: String = "",

    @field:Enumerated(EnumType.STRING)
    @field:JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @field:Column(name = "status", nullable = false, columnDefinition = "LOBBY_STATUS")
    var status: LobbyStatus = LobbyStatus.ACTIVE,

    @field:Column(name = "invite_code", nullable = false, length = 64)
    var inviteCode: String = "",

    @field:Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @field:Column(name = "closed_at")
    var closedAt: OffsetDateTime? = null
) {
    constructor() : this(
        id = null,
        name = "",
        game = "",
        difficulty = LobbyDifficulty.MEDIUM,
        durationMinutes = 0,
        maxAttempts = 0,
        gameOverText = "",
        status = LobbyStatus.ACTIVE,
        inviteCode = "",
        createdAt = OffsetDateTime.now(),
        closedAt = null
    )
}

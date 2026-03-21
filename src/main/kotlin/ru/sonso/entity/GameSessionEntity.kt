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
import ru.sonso.enumerable.SessionStatus
import java.time.OffsetDateTime

@Entity
@Table(name = "game_sessions")
data class GameSessionEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(name = "id", nullable = false, updatable = false)
    var id: Long? = null,

    @field:Column(name = "player_id", nullable = false)
    var playerId: Long = 0,

    @field:Column(name = "lobby_id", nullable = false)
    var lobbyId: Long = 0,

    @field:Column(name = "attempt_no", nullable = false)
    var attemptNo: Int = 0,

    @field:Column(name = "session_token", nullable = false, length = 128)
    var sessionToken: String = "",

    @field:Column(name = "started_at", nullable = false)
    var startedAt: OffsetDateTime = OffsetDateTime.now(),

    @field:Column(name = "finished_at")
    var finishedAt: OffsetDateTime? = null,

    @field:Column(name = "duration_seconds")
    var durationSeconds: Int? = null,

    @field:Column(name = "final_score")
    var finalScore: Int? = null,

    @field:Enumerated(EnumType.STRING)
    @field:JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @field:Column(name = "status", nullable = false, columnDefinition = "SESSION_STATUS")
    var status: SessionStatus = SessionStatus.STARTED
) {
    constructor() : this(
        id = null,
        playerId = 0,
        lobbyId = 0,
        attemptNo = 0,
        sessionToken = "",
        startedAt = OffsetDateTime.now(),
        finishedAt = null,
        durationSeconds = null,
        finalScore = null,
        status = SessionStatus.STARTED
    )
}

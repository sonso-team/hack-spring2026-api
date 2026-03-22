package ru.sonso.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sonso.entity.GameSessionEntity
import ru.sonso.enumerable.SessionStatus
import java.util.UUID

@Repository
interface GameSessionRepository : JpaRepository<GameSessionEntity, Long> {
    fun countByPlayerId(playerId: UUID): Long
    fun countByLobbyIdAndStatus(lobbyId: UUID, status: SessionStatus): Long
    fun findBySessionToken(sessionToken: String): GameSessionEntity?
    fun findByPlayerIdAndStatus(playerId: UUID, status: SessionStatus): List<GameSessionEntity>
    fun findByLobbyIdAndStatusInAndFinishedAtIsNotNull(lobbyId: UUID, statuses: Collection<SessionStatus>): List<GameSessionEntity>
}

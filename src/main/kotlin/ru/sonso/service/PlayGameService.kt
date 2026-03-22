package ru.sonso.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.sonso.dto.Snapshot
import ru.sonso.dto.request.FinishGameRequest
import ru.sonso.dto.request.StartGameRequest
import ru.sonso.dto.response.FinishGameResponse
import ru.sonso.dto.response.StartGameResponse
import ru.sonso.entity.GameSessionEntity
import ru.sonso.entity.LobbyEntity
import ru.sonso.entity.PlayerEntity
import ru.sonso.enumerable.LobbyStatus
import ru.sonso.enumerable.SessionStatus
import ru.sonso.mapper.toApiValue
import ru.sonso.repository.GameSessionRepository
import ru.sonso.repository.LobbyRepository
import ru.sonso.repository.PlayerRepository
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.math.abs

@Service
class PlayGameService(
    private val playerRepository: PlayerRepository,
    private val lobbyRepository: LobbyRepository,
    private val gameSessionRepository: GameSessionRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun start(request: StartGameRequest): StartGameResponse {
        logger.info("Starting game session, playerId={}", request.playerId)
        val player = playerRepository.findById(request.playerId)
            .orElseThrow { ResponseStatusException(HttpStatus.FORBIDDEN, "Player not found") }

        val lobby = lobbyRepository.findById(player.lobbyId)
            .orElseThrow { ResponseStatusException(HttpStatus.FORBIDDEN, "Lobby not found") }
        if (lobby.status != LobbyStatus.ACTIVE) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Lobby is closed")
        }

        if (gameSessionRepository.findByPlayerIdAndStatus(requirePlayerId(player), SessionStatus.STARTED).isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Player already has an active session")
        }

        val attemptsUsed = gameSessionRepository.countByPlayerId(requirePlayerId(player)).toInt()
        if (attemptsUsed >= lobby.maxAttempts) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No attempts left")
        }

        val session = gameSessionRepository.save(
            GameSessionEntity(
                playerId = requirePlayerId(player),
                lobbyId = requireLobbyId(lobby),
                attemptNo = attemptsUsed + 1,
                sessionToken = UUID.randomUUID().toString(),
                startedAt = OffsetDateTime.now(),
                status = SessionStatus.STARTED,
            )
        )

        return StartGameResponse(
            sessionToken = session.sessionToken,
            difficulty = lobby.difficulty?.toApiValue(),
            durationSeconds = lobby.durationMinutes * 60,
        ).also { logger.info("Game session started, token={}", session.sessionToken.take(8)) }
    }

    @Transactional
    fun finish(request: FinishGameRequest): FinishGameResponse {
        logger.info("Finishing game session")
        if (request.finalScore < 0) throw IllegalArgumentException("final_score must be >= 0")
        if (request.snapshots.isEmpty()) throw IllegalArgumentException("snapshots must not be empty")

        val session = gameSessionRepository.findBySessionToken(request.sessionToken.trim())
            ?: throw NoSuchElementException("Session not found")
        if (session.status != SessionStatus.STARTED) {
            throw IllegalArgumentException("Session is already finished")
        }

        val lobby = lobbyRepository.findById(session.lobbyId)
            .orElseThrow { NoSuchElementException("Lobby not found") }

        val suspicious = detectSuspiciousSession(
            request = request,
            session = session,
            expectedDurationSeconds = lobby.durationMinutes * 60,
        )

        val finishedAt = OffsetDateTime.now()
        session.finishedAt = finishedAt
        session.durationSeconds = Duration.between(session.startedAt, finishedAt).seconds.toInt().coerceAtLeast(0)
        session.finalScore = request.finalScore
        session.status = if (suspicious) SessionStatus.SUSPICIOUS else SessionStatus.COMPLETED

        val savedSession = gameSessionRepository.save(session)
        val rank = calculateRank(savedSession)

        return FinishGameResponse(
            score = request.finalScore,
            status = savedSession.status.toApiValue(),
            rank = rank,
        ).also { logger.info("Game session finished, sessionId={}, rank={}", savedSession.id, rank) }
    }

    private fun detectSuspiciousSession(
        request: FinishGameRequest,
        session: GameSessionEntity,
        expectedDurationSeconds: Int,
    ): Boolean {
        val snapshots = request.snapshots
        if (snapshots.any { it.score < 0 || it.kills < 0 }) {
            throw IllegalArgumentException("Snapshots contain negative values")
        }

        var suspicious = false

        val actualDurationSeconds = Duration.between(session.startedAt, OffsetDateTime.now())
            .seconds
            .toInt()
            .coerceAtLeast(0)
        if (abs(actualDurationSeconds - expectedDurationSeconds) > 5) {
            suspicious = true
        }

        suspicious = suspicious || !areSnapshotsChronologicalAndTimed(snapshots)
        suspicious = suspicious || !isScoreProgressValid(snapshots)

        if (snapshots.last().score != request.finalScore) {
            suspicious = true
        }

        return suspicious
    }

    private fun areSnapshotsChronologicalAndTimed(snapshots: List<Snapshot>): Boolean {
        if (snapshots.size < 2) return true
        for (index in 1 until snapshots.size) {
            val previous = snapshots[index - 1]
            val current = snapshots[index]
            val diff = current.timestamp - previous.timestamp
            if (diff <= 0L) return false
            if (diff !in 3000L..5000L) return false
        }
        return true
    }

    private fun isScoreProgressValid(snapshots: List<Snapshot>): Boolean {
        if (snapshots.size < 2) return true
        for (index in 1 until snapshots.size) {
            val previousScore = snapshots[index - 1].score
            val currentScore = snapshots[index].score
            val diff = currentScore - previousScore
            if (diff < 0) return false
            if (diff > 500) return false
        }
        return true
    }

    private fun calculateRank(session: GameSessionEntity): Int {
        val results = gameSessionRepository.findByLobbyIdAndStatusInAndFinishedAtIsNotNull(
            lobbyId = session.lobbyId,
            statuses = listOf(SessionStatus.COMPLETED, SessionStatus.SUSPICIOUS),
        ).filter {
            it.finalScore != null && it.durationSeconds != null
        }.sortedWith(
            compareByDescending<GameSessionEntity> { it.finalScore ?: 0 }
                .thenBy { it.durationSeconds ?: Int.MAX_VALUE }
                .thenBy { it.finishedAt ?: OffsetDateTime.MIN }
        )

        val sessionId = requireSessionId(session)
        return results.indexOfFirst { requireSessionId(it) == sessionId }
            .takeIf { it >= 0 }
            ?.plus(1)
            ?: results.size
    }

    private fun requireSessionId(session: GameSessionEntity): Long = checkNotNull(session.id) {
        "Session id is missing"
    }

    private fun requireLobbyId(lobby: LobbyEntity): UUID = checkNotNull(lobby.id) {
        "Lobby id is missing"
    }

    private fun requirePlayerId(player: PlayerEntity): UUID = checkNotNull(player.id) {
        "Player id is missing"
    }
}

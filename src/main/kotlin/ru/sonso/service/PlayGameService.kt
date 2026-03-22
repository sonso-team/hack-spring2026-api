package ru.sonso.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
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
import java.util.*
import kotlin.math.abs

@Service
class PlayGameService(
    private val playerRepository: PlayerRepository,
    private val lobbyRepository: LobbyRepository,
    private val gameSessionRepository: GameSessionRepository,
    private val playersInGameWsService: PlayersInGameWsService,
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
        playersInGameWsService.publishCurrentPlayersInGame()

        return StartGameResponse(
            sessionToken = session.sessionToken,
            difficulty = lobby.difficulty.toApiValue(),
            durationSeconds = lobby.durationMinutes * 60,
        ).also { logger.info("Game session started, token={}", session.sessionToken.take(8)) }
    }

    @Transactional
    fun finish(request: FinishGameRequest): FinishGameResponse {
        logger.info("Finishing game session")
        if (request.finalScore < 0) throw IllegalArgumentException("final_score must be >= 0")

        val session = gameSessionRepository.findBySessionToken(request.sessionToken.trim())
            ?: throw NoSuchElementException("Session not found")
        if (session.status != SessionStatus.STARTED) {
            throw IllegalArgumentException("Session is already finished")
        }

        val lobby = lobbyRepository.findById(session.lobbyId)
            .orElseThrow { NoSuchElementException("Lobby not found") }

        val suspicious = detectSuspiciousSession(
            session = session,
            expectedDurationSeconds = lobby.durationMinutes * 60,
        )

        val finishedAt = OffsetDateTime.now()
        session.finishedAt = finishedAt
        session.durationSeconds = Duration.between(session.startedAt, finishedAt).seconds.toInt().coerceAtLeast(0)
        session.finalScore = request.finalScore
        session.status = if (suspicious) SessionStatus.SUSPICIOUS else SessionStatus.COMPLETED

        val savedSession = gameSessionRepository.save(session)
        playersInGameWsService.publishCurrentPlayersInGame()
        val rank = calculateRank(savedSession)

        return FinishGameResponse(
            score = request.finalScore,
            status = savedSession.status.toApiValue(),
            rank = rank,
        ).also { logger.info("Game session finished, sessionId={}, rank={}", savedSession.id, rank) }
    }

    private fun detectSuspiciousSession(
        session: GameSessionEntity,
        expectedDurationSeconds: Int,
    ): Boolean {
        var suspicious = false

        val actualDurationSeconds = Duration.between(session.startedAt, OffsetDateTime.now())
            .seconds
            .toInt()
            .coerceAtLeast(0)
        if (abs(actualDurationSeconds - expectedDurationSeconds) > 5) {
            suspicious = true
        }

        return suspicious
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

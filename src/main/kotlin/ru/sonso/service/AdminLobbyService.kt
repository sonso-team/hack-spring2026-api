package ru.sonso.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.sonso.dto.SuccessResponse
import ru.sonso.dto.request.CreateLobbyRequest
import ru.sonso.dto.response.Lobby
import ru.sonso.entity.LobbyEntity
import ru.sonso.enumerable.LobbyStatus
import ru.sonso.mapper.toLobby
import ru.sonso.mapper.toLobbyEntity
import ru.sonso.repository.LobbyRepository
import ru.sonso.repository.PlayerRepository
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AdminLobbyService(
    private val lobbyRepository: LobbyRepository,
    private val playerRepository: PlayerRepository,
    private val playersInGameWsService: PlayersInGameWsService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getActiveLobby(): Lobby {
        logger.info("Fetching active lobby")
        val lobby = lobbyRepository.findFirstByStatusOrderByCreatedAtDesc(LobbyStatus.ACTIVE)
            ?: throw NoSuchElementException("Active lobby not found")
        return lobby.toLobby(
            playersCount = playerRepository.countByLobbyId(requireLobbyId(lobby)).toInt(),
            onlinePlayersCount = playersInGameWsService.getConnectedClientsCount(),
        )
            .also { logger.info("Active lobby fetched, id={}", it.id) }
    }

    @Transactional
    fun createLobby(request: CreateLobbyRequest): Lobby {
        logger.info("Creating lobby, name={}", request.name)
        validateCreateRequest(request)
        if (lobbyRepository.existsByStatus(LobbyStatus.ACTIVE)) {
            logger.warn("Lobby creation conflict: active lobby already exists")
            throw ResponseStatusException(HttpStatus.CONFLICT, "Active lobby already exists")
        }

        val savedLobby = lobbyRepository.save(request.toLobbyEntity(inviteCode = generateUniqueInviteCode()))
        playersInGameWsService.onLobbyActivated()
        return savedLobby.toLobby(
            playersCount = 0,
            onlinePlayersCount = playersInGameWsService.getConnectedClientsCount(),
        )
            .also { logger.info("Lobby created, id={}", it.id) }
    }

    @Transactional
    fun deleteLobby(): SuccessResponse {
        logger.info("Deleting active lobby")
        val activeLobby = lobbyRepository.findFirstByStatusOrderByCreatedAtDesc(LobbyStatus.ACTIVE)
        if (activeLobby != null) {
            lobbyRepository.delete(activeLobby)
            playersInGameWsService.onLobbyDeactivated()
            logger.info("Active lobby deleted, id={}", activeLobby.id)
            return SuccessResponse(success = true)
        }
        logger.info("No active lobby found for deletion")
        return SuccessResponse(success = false)
    }

    @Transactional
    fun toggleLobbyStatus(): Lobby {
        logger.info("Toggling lobby status")
        val lobby = lobbyRepository.findFirstByStatusOrderByCreatedAtDesc(LobbyStatus.ACTIVE)
            ?: lobbyRepository.findFirstByOrderByCreatedAtDesc()
            ?: throw NoSuchElementException("Lobby not found")

        val updated = when (lobby.status) {
            LobbyStatus.ACTIVE -> lobby.copy(
                status = LobbyStatus.CLOSED,
                closedAt = OffsetDateTime.now(),
            )

            LobbyStatus.CLOSED -> {
                if (lobbyRepository.existsByStatus(LobbyStatus.ACTIVE)) {
                    logger.warn("Toggle conflict: another active lobby already exists")
                    throw ResponseStatusException(HttpStatus.CONFLICT, "Another active lobby already exists")
                }
                lobby.copy(status = LobbyStatus.ACTIVE, closedAt = null)
            }
        }

        val saved = lobbyRepository.save(updated)
        when (saved.status) {
            LobbyStatus.ACTIVE -> playersInGameWsService.onLobbyActivated()
            LobbyStatus.CLOSED -> playersInGameWsService.onLobbyDeactivated()
        }
        return saved.toLobby(
            playersCount = playerRepository.countByLobbyId(requireLobbyId(saved)).toInt(),
            onlinePlayersCount = playersInGameWsService.getConnectedClientsCount(),
        )
            .also { logger.info("Lobby status toggled, id={}, status={}", it.id, it.status) }
    }

    private fun validateCreateRequest(request: CreateLobbyRequest) {
        if (request.name.isBlank()) throw IllegalArgumentException("name is required")
        if (request.game.lowercase() != "ddos_ninja") throw IllegalArgumentException("Unsupported game")
        if (request.durationMinutes < 1) throw IllegalArgumentException("duration_minutes must be >= 1")
        if (request.maxAttempts < 1) throw IllegalArgumentException("max_attempts must be >= 1")
        if (request.gameOverText.isBlank()) throw IllegalArgumentException("game_over_text is required")
    }

    private fun generateUniqueInviteCode(): String {
        repeat(10) {
            val code = UUID.randomUUID().toString().replace("-", "").take(8)
            if (lobbyRepository.findByInviteCode(code) == null) {
                return code
            }
        }
        throw IllegalStateException("Unable to generate unique invite code")
    }

    private fun requireLobbyId(lobby: LobbyEntity): UUID = checkNotNull(lobby.id) {
        "Lobby id is missing"
    }
}

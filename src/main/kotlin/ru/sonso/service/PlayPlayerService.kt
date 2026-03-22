package ru.sonso.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sonso.dto.request.RegisterPlayerRequest
import ru.sonso.dto.response.PlayerStateResponse
import ru.sonso.entity.LobbyEntity
import ru.sonso.entity.PlayerEntity
import ru.sonso.enumerable.LobbyStatus
import ru.sonso.mapper.toPlayerEntity
import ru.sonso.repository.GameSessionRepository
import ru.sonso.repository.LobbyRepository
import ru.sonso.repository.PlayerRepository
import java.util.UUID
import kotlin.math.max

@Service
class PlayPlayerService(
    private val lobbyRepository: LobbyRepository,
    private val playerRepository: PlayerRepository,
    private val gameSessionRepository: GameSessionRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun register(request: RegisterPlayerRequest): PlayerStateResponse {
        logger.info("Registering player, phone={}, inviteCode={}", request.phone, request.inviteCode)
        validateRegisterRequest(request)

        val lobby = lobbyRepository.findByInviteCode(request.inviteCode.trim())
            ?.takeIf { it.status == LobbyStatus.ACTIVE }
            ?: throw NoSuchElementException("Lobby not found or closed")

        val lobbyId = requireLobbyId(lobby)
        val existingPlayer = playerRepository.findByLobbyIdAndPhone(lobbyId, request.phone.trim())
            ?: playerRepository.findByLobbyIdAndEmail(lobbyId, request.email.trim())

        val player = existingPlayer ?: playerRepository.save(request.toPlayerEntity(lobbyId = lobbyId))
        return buildPlayerStateResponse(player, lobby, registered = true)
            .also { logger.info("Player register completed, playerId={}, canPlay={}", it.playerId, it.canPlay) }
    }

    @Transactional(readOnly = true)
    fun getStatus(inviteCode: String, phone: String): PlayerStateResponse {
        logger.info("Checking player status, inviteCode={}, phone={}", inviteCode, phone)
        val lobby = lobbyRepository.findByInviteCode(inviteCode.trim())
            ?.takeIf { it.status == LobbyStatus.ACTIVE }
            ?: return PlayerStateResponse(
                playerId = null,
                registered = false,
                canPlay = false,
                attemptsLeft = 0,
            )

        val player = playerRepository.findByLobbyIdAndPhone(requireLobbyId(lobby), phone.trim())
            ?: return PlayerStateResponse(
                playerId = null,
                registered = false,
                canPlay = false,
                attemptsLeft = 0,
            )

        return buildPlayerStateResponse(player, lobby, registered = true)
            .also { logger.info("Player status resolved, playerId={}, canPlay={}", it.playerId, it.canPlay) }
    }

    private fun buildPlayerStateResponse(player: PlayerEntity, lobby: LobbyEntity, registered: Boolean): PlayerStateResponse {
        val attemptsLeft = calculateAttemptsLeft(player = player, lobby = lobby)
        return PlayerStateResponse(
            playerId = requirePlayerId(player),
            registered = registered,
            canPlay = attemptsLeft > 0 && lobby.status == LobbyStatus.ACTIVE,
            attemptsLeft = attemptsLeft,
        )
    }

    private fun calculateAttemptsLeft(player: PlayerEntity, lobby: LobbyEntity): Int {
        val attemptsUsed = gameSessionRepository.countByPlayerId(requirePlayerId(player)).toInt()
        return max(0, lobby.maxAttempts - attemptsUsed)
    }

    private fun validateRegisterRequest(request: RegisterPlayerRequest) {
        if (request.firstName.isBlank()) throw IllegalArgumentException("first_name is required")
        if (request.lastName.isBlank()) throw IllegalArgumentException("last_name is required")
        if (request.phone.isBlank()) throw IllegalArgumentException("phone is required")
        if (request.email.isBlank()) throw IllegalArgumentException("email is required")
        if (request.inviteCode.isBlank()) throw IllegalArgumentException("invite_code is required")
    }

    private fun requireLobbyId(lobby: LobbyEntity): UUID = checkNotNull(lobby.id) {
        "Lobby id is missing"
    }

    private fun requirePlayerId(player: PlayerEntity): UUID = checkNotNull(player.id) {
        "Player id is missing"
    }
}

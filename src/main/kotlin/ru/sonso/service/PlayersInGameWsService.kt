package ru.sonso.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import ru.sonso.dto.ws.PlayersInGameMessage
import ru.sonso.enumerable.LobbyStatus
import ru.sonso.enumerable.SessionStatus
import ru.sonso.repository.GameSessionRepository
import ru.sonso.repository.LobbyRepository
import java.util.concurrent.ConcurrentHashMap

@Service
class PlayersInGameWsService(
    private val lobbyRepository: LobbyRepository,
    private val gameSessionRepository: GameSessionRepository,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val sessions = ConcurrentHashMap.newKeySet<WebSocketSession>()

    @Volatile
    private var lobbyActive = false

    @EventListener(ApplicationReadyEvent::class)
    fun initLobbyState() {
        lobbyActive = lobbyRepository.existsByStatus(LobbyStatus.ACTIVE)
        logger.info("Players WS initialized, lobbyActive={}", lobbyActive)
    }

    fun handleConnected(session: WebSocketSession) {
        if (!lobbyActive) {
            logger.info("Players WS rejected connection, sessionId={}, reason=no active lobby", session.id)
            runCatching {
                session.close(CloseStatus.POLICY_VIOLATION.withReason("Active lobby is required"))
            }
            return
        }

        sessions.add(session)
        logger.info("Players WS client connected, sessionId={}, clients={}", session.id, sessions.size)
        sendPlayersInGame(session, calculatePlayersInGame())
    }

    fun handleDisconnected(session: WebSocketSession) {
        sessions.remove(session)
        logger.info("Players WS client disconnected, sessionId={}, clients={}", session.id, sessions.size)
    }

    fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        sessions.remove(session)
        logger.warn("Players WS transport error, sessionId={}", session.id, exception)
        runCatching {
            if (session.isOpen) {
                session.close(CloseStatus.SERVER_ERROR)
            }
        }
    }

    fun onLobbyActivated() {
        lobbyActive = true
        logger.info("Players WS enabled")
        publishCurrentPlayersInGame()
    }

    fun onLobbyDeactivated() {
        lobbyActive = false
        logger.info("Players WS disabled")
        broadcastPlayersInGame(playersInGame = 0)
        closeAllSessions(CloseStatus.NORMAL.withReason("Lobby is inactive"))
    }

    fun publishCurrentPlayersInGame() {
        if (!lobbyActive) return
        broadcastPlayersInGame(calculatePlayersInGame())
    }

    fun getConnectedClientsCount(): Int {
        sessions.removeIf { !it.isOpen }
        return sessions.size
    }

    private fun calculatePlayersInGame(): Int {
        val lobby = lobbyRepository.findFirstByStatusOrderByCreatedAtDesc(LobbyStatus.ACTIVE) ?: return 0
        val lobbyId = lobby.id ?: return 0
        return gameSessionRepository.countByLobbyIdAndStatus(lobbyId, SessionStatus.STARTED).toInt()
    }

    private fun broadcastPlayersInGame(playersInGame: Int) {
        if (sessions.isEmpty()) return

        val message = buildMessage(playersInGame)
        sessions.toList().forEach { session ->
            sendMessage(session, message)
        }
        logger.info("Players WS broadcast, playersInGame={}, clients={}", playersInGame, sessions.size)
    }

    private fun sendPlayersInGame(session: WebSocketSession, playersInGame: Int) {
        sendMessage(session, buildMessage(playersInGame))
    }

    private fun buildMessage(playersInGame: Int): TextMessage {
        val payload = PlayersInGameMessage(playersInGame = playersInGame)
        return TextMessage(objectMapper.writeValueAsString(payload))
    }

    private fun sendMessage(session: WebSocketSession, message: TextMessage) {
        if (!session.isOpen) {
            sessions.remove(session)
            return
        }

        runCatching {
            synchronized(session) {
                if (session.isOpen) {
                    session.sendMessage(message)
                }
            }
        }.onFailure { exception ->
            sessions.remove(session)
            logger.warn("Players WS send failed, sessionId={}", session.id, exception)
            runCatching {
                if (session.isOpen) {
                    session.close(CloseStatus.SERVER_ERROR)
                }
            }
        }
    }

    private fun closeAllSessions(status: CloseStatus) {
        sessions.toList().forEach { session ->
            sessions.remove(session)
            runCatching {
                if (session.isOpen) {
                    session.close(status)
                }
            }
        }
    }
}

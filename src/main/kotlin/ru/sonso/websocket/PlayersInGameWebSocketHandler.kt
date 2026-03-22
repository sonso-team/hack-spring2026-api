package ru.sonso.websocket

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import ru.sonso.service.PlayersInGameWsService

@Component
class PlayersInGameWebSocketHandler(
    private val playersInGameWsService: PlayersInGameWsService,
) : TextWebSocketHandler() {
    override fun afterConnectionEstablished(session: WebSocketSession) {
        playersInGameWsService.handleConnected(session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        playersInGameWsService.handleDisconnected(session)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        playersInGameWsService.handleTransportError(session, exception)
    }
}

package ru.sonso.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import ru.sonso.websocket.PlayersInGameWebSocketHandler

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val playersInGameWebSocketHandler: PlayersInGameWebSocketHandler,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(playersInGameWebSocketHandler, "/ws/play")
            .setAllowedOrigins("http://localhost:5173", "https://hack.kinoko.su")
    }
}

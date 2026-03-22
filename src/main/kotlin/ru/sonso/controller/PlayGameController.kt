package ru.sonso.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sonso.dto.request.FinishGameRequest
import ru.sonso.dto.request.StartGameRequest
import ru.sonso.dto.response.FinishGameResponse
import ru.sonso.dto.response.StartGameResponse
import ru.sonso.service.PlayGameService

@RestController
@RequestMapping("/api/play/game")
class PlayGameController(
    private val playGameService: PlayGameService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/start")
    fun start(@RequestBody request: StartGameRequest): ResponseEntity<StartGameResponse> {
        logger.info("POST /api/play/game/start requested, playerId={}", request.playerId)
        val response = playGameService.start(request)
        logger.info("POST /api/play/game/start completed")
        return ResponseEntity.ok(response)
    }

    @PostMapping("/finish")
    fun finish(@RequestBody request: FinishGameRequest): ResponseEntity<FinishGameResponse> {
        logger.info("POST /api/play/game/finish requested")
        val response = playGameService.finish(request)
        logger.info("POST /api/play/game/finish completed, rank={}", response.rank)
        return ResponseEntity.ok(response)
    }
}

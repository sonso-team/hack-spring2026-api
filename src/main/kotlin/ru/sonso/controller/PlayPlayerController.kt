package ru.sonso.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.sonso.dto.request.RegisterPlayerRequest
import ru.sonso.dto.response.PlayerStateResponse
import ru.sonso.service.PlayPlayerService

@RestController
@RequestMapping("/api/play")
class PlayPlayerController(
    private val playPlayerService: PlayPlayerService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterPlayerRequest): ResponseEntity<PlayerStateResponse> {
        logger.info("POST /api/play/register requested, phone={}", request.phone)
        val response = playPlayerService.register(request)
        logger.info("POST /api/play/register completed, registered={}, canPlay={}", response.registered, response.canPlay)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/status")
    fun getStatus(
        @RequestParam("invite_code") inviteCode: String,
        @RequestParam phone: String,
    ): ResponseEntity<PlayerStateResponse> {
        logger.info("GET /api/play/status requested, inviteCode={}, phone={}", inviteCode, phone)
        val response = playPlayerService.getStatus(inviteCode = inviteCode, phone = phone)
        logger.info("GET /api/play/status completed, registered={}, canPlay={}", response.registered, response.canPlay)
        return ResponseEntity.ok(response)
    }
}

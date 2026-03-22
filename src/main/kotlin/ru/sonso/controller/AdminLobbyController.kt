package ru.sonso.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sonso.dto.SuccessResponse
import ru.sonso.dto.request.CreateLobbyRequest
import ru.sonso.dto.response.Lobby
import ru.sonso.service.AdminLobbyService

@RestController
@RequestMapping("/api/admin/lobby")
class AdminLobbyController(
    private val adminLobbyService: AdminLobbyService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getActiveLobby(): ResponseEntity<Lobby> {
        logger.info("GET /api/admin/lobby requested")
        val lobby = adminLobbyService.getActiveLobby()
        logger.info("GET /api/admin/lobby completed, lobbyId={}", lobby.id)
        return ResponseEntity.ok(lobby)
    }

    @PostMapping
    fun createLobby(@RequestBody request: CreateLobbyRequest): ResponseEntity<Lobby> {
        logger.info("POST /api/admin/lobby requested, name={}", request.name)
        val lobby = adminLobbyService.createLobby(request)
        logger.info("POST /api/admin/lobby completed, lobbyId={}", lobby.id)
        return ResponseEntity.ok(lobby)
    }

    @DeleteMapping
    fun deleteLobby(): ResponseEntity<SuccessResponse> {
        logger.info("DELETE /api/admin/lobby requested")
        val result = adminLobbyService.deleteLobby()
        logger.info("DELETE /api/admin/lobby completed, success={}", result.success)
        return ResponseEntity.ok(result)
    }

    @PatchMapping("/toggle")
    fun toggleLobbyStatus(): ResponseEntity<Lobby> {
        logger.info("PATCH /api/admin/lobby/toggle requested")
        val lobby = adminLobbyService.toggleLobbyStatus()
        logger.info("PATCH /api/admin/lobby/toggle completed, lobbyId={}, status={}", lobby.id, lobby.status)
        return ResponseEntity.ok(lobby)
    }
}

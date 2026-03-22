package ru.sonso.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.sonso.dto.PlayerResult
import ru.sonso.dto.response.RandomWinner
import ru.sonso.service.AdminResultsService

@RestController
@RequestMapping("/api/admin/lobby")
class AdminResultsController(
    private val adminResultsService: AdminResultsService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/results")
    fun getResults(
        @RequestParam(required = false) search: String?,
        @RequestParam(name = "sort_by", required = false) sortBy: String?,
        @RequestParam(required = false) order: String?,
    ): ResponseEntity<List<PlayerResult>> {
        logger.info(
            "GET /api/admin/lobby/results requested, search={}, sortBy={}, order={}",
            search,
            sortBy,
            order,
        )
        val results = adminResultsService.getResults(search = search, sortBy = sortBy, order = order)
        logger.info("GET /api/admin/lobby/results completed, count={}", results.size)
        return ResponseEntity.ok(results)
    }

    @GetMapping("/results/export")
    fun exportResults(): ResponseEntity<String> {
        logger.info("GET /api/admin/lobby/results/export requested")
        val payload = adminResultsService.exportResultsCsv()
        logger.info("GET /api/admin/lobby/results/export completed, size={}", payload.length)
        return ResponseEntity.ok()
            .contentType(MediaType("text", "csv"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"results.csv\"")
            .body(payload)
    }

    @GetMapping("/random")
    fun getRandomWinner(): ResponseEntity<RandomWinner> {
        logger.info("GET /api/admin/lobby/random requested")
        val winner = adminResultsService.getRandomWinner()
        logger.info("GET /api/admin/lobby/random completed, playerId={}", winner.playerId)
        return ResponseEntity.ok(winner)
    }
}

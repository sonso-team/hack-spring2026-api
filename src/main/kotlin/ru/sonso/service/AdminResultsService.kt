package ru.sonso.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sonso.dto.PlayerResult
import ru.sonso.dto.response.RandomWinner
import ru.sonso.entity.GameSessionEntity
import ru.sonso.entity.LobbyEntity
import ru.sonso.entity.PlayerEntity
import ru.sonso.enumerable.LobbyStatus
import ru.sonso.enumerable.SessionStatus
import ru.sonso.mapper.toPlayerResult
import ru.sonso.repository.GameSessionRepository
import ru.sonso.repository.LobbyRepository
import ru.sonso.repository.PlayerRepository
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.random.Random

@Service
class AdminResultsService(
    private val lobbyRepository: LobbyRepository,
    private val playerRepository: PlayerRepository,
    private val gameSessionRepository: GameSessionRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getResults(search: String?, sortBy: String?, order: String?): List<PlayerResult> {
        logger.info("Fetching lobby results, search={}, sortBy={}, order={}", search, sortBy, order)
        val lobby = requireLatestLobby()
        val rows = fetchRowsForLobby(lobbyId = requireLobbyId(lobby), search = search)
        if (rows.isEmpty()) {
            logger.info("No rows found for lobby results")
            return emptyList()
        }

        val rankBySessionId = buildRankMap(rows)
        val sorted = sortRows(rows, sortBy = sortBy, order = order)

        return sorted.map { row ->
            row.session.toPlayerResult(
                player = row.player,
                rank = rankBySessionId[requireSessionId(row.session)] ?: 0,
            )
        }.also { logger.info("Lobby results fetched, count={}", it.size) }
    }

    @Transactional(readOnly = true)
    fun exportResultsCsv(): String {
        logger.info("Exporting lobby results to CSV")
        val results = getResults(search = null, sortBy = "score", order = "desc")
        val lines = mutableListOf("rank,player_id,first_name,last_name,phone,email,score,duration_seconds,status,played_at")
        results.forEach { item ->
            lines.add(
                listOf(
                    item.rank,
                    item.playerId,
                    escapeCsv(item.firstName),
                    escapeCsv(item.lastName),
                    escapeCsv(item.phone),
                    escapeCsv(item.email),
                    item.score,
                    item.durationSeconds,
                    item.status,
                    item.playedAt,
                ).joinToString(",")
            )
        }
        return lines.joinToString("\n").also { logger.info("CSV export built, rows={}", results.size) }
    }

    @Transactional(readOnly = true)
    fun getRandomWinner(): RandomWinner {
        logger.info("Selecting random winner")
        val lobby = requireLatestLobby()
        val rows = fetchRowsForLobby(
            lobbyId = requireLobbyId(lobby),
            search = null,
            allowedStatuses = setOf(SessionStatus.COMPLETED, SessionStatus.SUSPICIOUS),
        )
        if (rows.isEmpty()) {
            throw NoSuchElementException("No completed results found")
        }

        val bestByPlayer = rows
            .groupBy { requirePlayerId(it.player) }
            .mapValues { (_, items) ->
                items.sortedWith(
                    compareByDescending<Row> { it.session.finalScore ?: 0 }
                        .thenBy { it.session.durationSeconds ?: Int.MAX_VALUE }
                        .thenBy { it.session.finishedAt ?: OffsetDateTime.MIN }
                ).first()
            }
            .values
            .toList()

        val winner = bestByPlayer[Random.nextInt(bestByPlayer.size)]
        return RandomWinner(
            playerId = requirePlayerId(winner.player),
            firstName = winner.player.firstName,
            lastName = winner.player.lastName,
            score = winner.session.finalScore ?: 0,
        ).also { logger.info("Random winner selected, playerId={}", it.playerId) }
    }

    private fun requireLatestLobby(): LobbyEntity = lobbyRepository.findFirstByStatusOrderByCreatedAtDesc(
        status = LobbyStatus.ACTIVE,
    ) ?: lobbyRepository.findFirstByOrderByCreatedAtDesc()
        ?: throw NoSuchElementException("Lobby not found")

    private fun fetchRowsForLobby(
        lobbyId: UUID,
        search: String?,
        allowedStatuses: Set<SessionStatus> = setOf(SessionStatus.COMPLETED, SessionStatus.SUSPICIOUS),
    ): List<Row> {
        val sessions = gameSessionRepository.findByLobbyIdAndStatusInAndFinishedAtIsNotNull(
            lobbyId = lobbyId,
            statuses = allowedStatuses,
        ).filter {
            it.finalScore != null && it.durationSeconds != null
        }
        if (sessions.isEmpty()) return emptyList()

        val players = playerRepository.findAllById(sessions.map { it.playerId }.distinct())
            .associateBy { requirePlayerId(it) }

        val rows = sessions.mapNotNull { session ->
            val player = players[session.playerId] ?: return@mapNotNull null
            Row(session = session, player = player)
        }

        val normalizedSearch = search?.trim()?.takeIf { it.isNotEmpty() }?.lowercase()
        return if (normalizedSearch == null) {
            rows
        } else {
            rows.filter { row ->
                row.player.firstName.lowercase().contains(normalizedSearch) ||
                    row.player.lastName.lowercase().contains(normalizedSearch)
            }
        }
    }

    private fun buildRankMap(rows: List<Row>): Map<Long, Int> {
        val rankedRows = rows.sortedWith(
            compareByDescending<Row> { it.session.finalScore ?: 0 }
                .thenBy { it.session.durationSeconds ?: Int.MAX_VALUE }
                .thenBy { it.session.finishedAt ?: OffsetDateTime.MIN }
        )
        return rankedRows.mapIndexed { index, row -> requireSessionId(row.session) to (index + 1) }.toMap()
    }

    private fun sortRows(rows: List<Row>, sortBy: String?, order: String?): List<Row> {
        val normalizedSort = sortBy?.lowercase() ?: "score"
        val normalizedOrder = order?.lowercase() ?: "desc"
        if (normalizedOrder != "asc" && normalizedOrder != "desc") {
            throw IllegalArgumentException("Invalid order value")
        }

        return when (normalizedSort) {
            "score" -> if (normalizedOrder == "asc") {
                rows.sortedWith(
                    compareBy<Row> { it.session.finalScore ?: 0 }
                        .thenBy { it.session.durationSeconds ?: Int.MAX_VALUE }
                        .thenBy { it.session.finishedAt ?: OffsetDateTime.MIN }
                )
            } else {
                rows.sortedWith(
                    compareByDescending<Row> { it.session.finalScore ?: 0 }
                        .thenBy { it.session.durationSeconds ?: Int.MAX_VALUE }
                        .thenBy { it.session.finishedAt ?: OffsetDateTime.MIN }
                )
            }

            "duration" -> if (normalizedOrder == "asc") {
                rows.sortedWith(
                    compareBy<Row> { it.session.durationSeconds ?: Int.MAX_VALUE }
                        .thenByDescending { it.session.finalScore ?: 0 }
                        .thenBy { it.session.finishedAt ?: OffsetDateTime.MIN }
                )
            } else {
                rows.sortedWith(
                    compareByDescending<Row> { it.session.durationSeconds ?: Int.MAX_VALUE }
                        .thenByDescending { it.session.finalScore ?: 0 }
                        .thenBy { it.session.finishedAt ?: OffsetDateTime.MIN }
                )
            }

            "created_at" -> if (normalizedOrder == "asc") {
                rows.sortedBy { it.session.finishedAt ?: OffsetDateTime.MIN }
            } else {
                rows.sortedByDescending { it.session.finishedAt ?: OffsetDateTime.MIN }
            }

            else -> throw IllegalArgumentException("Invalid sort_by value")
        }
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun requireLobbyId(lobby: LobbyEntity): UUID = checkNotNull(lobby.id) {
        "Lobby id is missing"
    }

    private fun requirePlayerId(player: PlayerEntity): UUID = checkNotNull(player.id) {
        "Player id is missing"
    }

    private fun requireSessionId(session: GameSessionEntity): Long = checkNotNull(session.id) {
        "Session id is missing"
    }

    private data class Row(
        val session: GameSessionEntity,
        val player: PlayerEntity,
    )
}

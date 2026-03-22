package ru.sonso.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sonso.entity.PlayerEntity
import java.util.*

@Repository
interface PlayerRepository : JpaRepository<PlayerEntity, UUID> {
    fun findByLobbyIdAndPhone(lobbyId: UUID, phone: String): PlayerEntity?
    fun findByLobbyIdAndEmail(lobbyId: UUID, email: String): PlayerEntity?
    fun countByLobbyId(lobbyId: UUID): Long
}

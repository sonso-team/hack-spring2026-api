package ru.sonso.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sonso.entity.LobbyEntity
import ru.sonso.enumerable.LobbyStatus
import java.util.UUID

@Repository
interface LobbyRepository : JpaRepository<LobbyEntity, UUID> {
    fun findFirstByStatusOrderByCreatedAtDesc(status: LobbyStatus): LobbyEntity?
    fun existsByStatus(status: LobbyStatus): Boolean
    fun findByInviteCode(inviteCode: String): LobbyEntity?
    fun findFirstByOrderByCreatedAtDesc(): LobbyEntity?
}

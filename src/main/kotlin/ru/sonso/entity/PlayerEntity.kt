package ru.sonso.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "players")
data class PlayerEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @field:Column(name = "lobby_id", nullable = false)
    var lobbyId: UUID = UUID(0L, 0L),

    @field:Column(name = "first_name", nullable = false, length = 100)
    var firstName: String = "",

    @field:Column(name = "last_name", nullable = false, length = 100)
    var lastName: String = "",

    @field:Column(name = "phone", nullable = false, length = 32)
    var phone: String = "",

    @field:Column(name = "email", nullable = false, length = 255)
    var email: String = "",

    @field:Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
) {
    constructor() : this(
        id = null,
        lobbyId = UUID(0L, 0L),
        firstName = "",
        lastName = "",
        phone = "",
        email = "",
        createdAt = OffsetDateTime.now()
    )
}
